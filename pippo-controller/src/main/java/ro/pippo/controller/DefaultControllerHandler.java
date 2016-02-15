/*
 * Copyright (C) 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.controller.extractor.ArgumentExtractor;
import ro.pippo.controller.extractor.CollectionExtractor;
import ro.pippo.controller.extractor.ConfigurableExtractor;
import ro.pippo.controller.extractor.FileItemExtractor;
import ro.pippo.controller.extractor.NamedExtractor;
import ro.pippo.controller.extractor.SuffixExtractor;
import ro.pippo.controller.extractor.TypedExtractor;
import ro.pippo.controller.util.ClassUtils;
import ro.pippo.controller.util.ControllerUtils;
import ro.pippo.core.ContentTypeEngines;
import ro.pippo.core.FileItem;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.Messages;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteMatch;
import ro.pippo.core.util.LangUtils;
import ro.pippo.core.util.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * ControllerHandler executes controller methods.
 *
 * @author James Moger
 */
public class DefaultControllerHandler implements ControllerHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultControllerHandler.class);

    protected final Class<? extends Controller> controllerClass;
    protected final String methodName;
    protected final Method method;
    protected final List<RouteHandler<Context>> routeInterceptors;
    protected final List<String> declaredConsumes;
    protected final List<String> declaredProduces;
    protected final Collection<Return> declaredReturns;
    protected final Set<String> contentTypeSuffixes;
    protected final boolean isNoCache;
    protected ArgumentExtractor[] extractors;
    protected String[] patterns;

    public DefaultControllerHandler(Class<? extends Controller> controllerClass, String methodName) {
        this.controllerClass = controllerClass;
        this.methodName = methodName;

        // TODO
//        this.controllerProvider = injector.getProvider(controllerClass);
        this.method = findMethod(controllerClass, methodName);
        // TODO
//        this.messages = injector.getInstance(Messages.class);

        // TODO
//        Preconditions.checkNotNull(method, "Failed to find method '%s'", Util.toString(controllerClass, methodName));
        log.trace("Obtained method for '{}'", LangUtils.toString(method));

        this.routeInterceptors = new ArrayList<>();
        for (Class<? extends RouteHandler<Context>> handlerClass : ControllerUtils.collectRouteInterceptors(method)) {
            RouteHandler<Context> handler = injector.getInstance(handlerClass);
            this.routeInterceptors.add(handler);
        }

        // TODO
//        ContentTypeEngines engines = injector.getInstance(ContentTypeEngines.class);

        this.declaredConsumes = ControllerUtils.getConsumes(method);
        validateConsumes(engines.getContentTypes());

        this.declaredProduces = ControllerUtils.getProduces(method);
        validateProduces(engines.getContentTypes());

        this.declaredReturns = ControllerUtils.getReturns(method);
        validateDeclaredReturns();

        this.contentTypeSuffixes = configureContentTypeSuffixes(engines);
        configureMethodArgs(injector);

        this.isNoCache = ClassUtils.getAnnotation(method, NoCache.class) != null;
    }

    public Class<? extends Controller> getControllerClass() {
        return controllerClass;
    }

    @Override
    public String getControllerMethodName() {
        return methodName;
    }

    @Override
    public Method getControllerMethod() {
        return method;
    }

    public List<String> getDeclaredConsumes() {
        return declaredConsumes;
    }

    public List<String> getDeclaredProduces() {
        return declaredProduces;
    }

    public Collection<Return> getDeclaredReturns() {
        return declaredReturns;
    }

    @Override
    public void handle(Context context) {
        try {
            if (!canConsume(context)) {
                context.next();

                return;
            }

            log.trace("Processing '{}' RouteInterceptors", LangUtils.toString(method));
            int preInterceptStatus = context.getResponse().getStatus();
            processRouteInterceptors(context);
            int postInterceptStatus = context.getResponse().getStatus();
            if (context.getResponse().isCommitted()) {
                log.debug("Response committed by RouteInterceptor");
                context.next();

                return;
            } else if (preInterceptStatus != postInterceptStatus && postInterceptStatus >= 300) {
                log.debug("RouteInterceptor set status code to {}, committing response",
                        context.getResponse().getStatus());
                context.getResponse().commit();
                context.next();

                return;
            }

            log.trace("Preparing '{}' arguments from request", LangUtils.toString(method));
            Object[] args = prepareMethodArgs(context);

            log.trace("Invoking '{}'", LangUtils.toString(method));
            Controller controller = controllerProvider.get();
            controller.setContext(context);

            specifyCacheControls(context);
            specifyContentType(context);

            Object result = method.invoke(controller, args);

            if (context.getResponse().isCommitted()) {
                log.debug("Response committed in {}", LangUtils.toString(method));
            } else {
                if (Void.class == method.getReturnType()) {
                    // nothing to return, prepare declared Return for Void type
                    for (Return declaredReturn : declaredReturns) {
                        if (Void.class == declaredReturn.onResult()) {
                            context.status(declaredReturn.code());
                            validateResponseHeaders(declaredReturn, context);
                            break;
                        }
                    }
                } else {
                    // method declares a Return Type
                    if (result == null) {
                        // Null Result, prepare a NOT FOUND (404)
                        context.getResponse().notFound();

                        for (Return declaredReturn : declaredReturns) {
                            if (declaredReturn.code() == HttpConstants.StatusCode.NOT_FOUND) {
                                String message = declaredReturn.description();

                                if (!StringUtils.isNullOrEmpty(declaredReturn.descriptionKey())) {
                                    // retrieve localized message, fallback to declared message
                                    message = messages.getWithDefault(declaredReturn.descriptionKey(), message, context);
                                }

                                if (!StringUtils.isNullOrEmpty(message)) {
                                    context.setLocal("message", message);
                                }

                                validateResponseHeaders(declaredReturn, context);
                                break;
                            }
                        }
                    } else {
                        // send returned result
                        Class<?> resultClass = result.getClass();
                        for (Return declaredReturn : declaredReturns) {
                            if (declaredReturn.onResult().isAssignableFrom(resultClass)) {
                                context.status(declaredReturn.code());
                                validateResponseHeaders(declaredReturn, context);
                                break;
                            }
                        }

                        if (result instanceof CharSequence) {
                            // send a charsequence (e.g. pre-formatted JSON, XML, YAML, etc)
                            CharSequence charSequence = (CharSequence) result;
                            context.send(charSequence);
                        } else if (result instanceof File) {
                            // stream a File resource
                            File file = (File) result;
                            context.send(file);
                        } else {
                            // send an object using a ContentTypeEngine
                            context.send(result);
                        }
                    }
                }
            }

            context.next();
        } catch (InvocationTargetException e) {
            // handles exceptions thrown within the proxied controller method
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                Exception target = (Exception) t;
                handleDeclaredThrownException(target, method, context);
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                log.error("Failed to handle controller method exception", t);
            }
        } catch (Exception e) {
            // handles exceptions thrown within this handle() method
            handleDeclaredThrownException(e, method, context);
        }
    }

    /**
     * Finds the named controller method.
     *
     * @param controllerClass
     * @param name
     * @return the controller method or null
     */
    protected Method findMethod(Class<?> controllerClass, String name) {
        // identify first method which matches the name
        Method controllerMethod = null;
        for (Method method : controllerClass.getMethods()) {
            if (method.getName().equals(name)) {
                if (controllerMethod == null) {
                    controllerMethod = method;
                } else {
                    throw new PippoRuntimeException("Found overloaded controller method '{}'. Method names must be unique!",
                            LangUtils.toString(method));
                }
            }
        }

        return controllerMethod;
    }

    /**
     * Configures the content-type suffixes
     *
     * @param engines
     * @return acceptable content-type suffixes
     */
    protected Set<String> configureContentTypeSuffixes(ContentTypeEngines engines) {
        if (null == ClassUtils.getAnnotation(method, ContentTypeBySuffix.class)) {
            return Collections.emptySet();
        }

        Set<String> suffixes = new TreeSet<>();
        for (String suffix : engines.getContentTypeSuffixes()) {
            String contentType = engines.getContentTypeEngine(suffix).getContentType();
            if (declaredProduces.contains(contentType)) {
                suffixes.add(suffix);
            }
        }

        return suffixes;
    }

    /**
     * Configures the controller method arguments.
     *
     * @param injector
     */
    protected void configureMethodArgs(Injector injector) {
        Class<?>[] types = method.getParameterTypes();
        extractors = new ArgumentExtractor[types.length];
        patterns = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            final Parameter parameter = method.getParameters()[i];
            final Class<? extends Collection> collectionType;
            final Class<?> objectType;
            if (Collection.class.isAssignableFrom(types[i])) {
                collectionType = (Class<? extends Collection>) types[i];
                objectType = getParameterGenericType(parameter);
            } else {
                collectionType = null;
                objectType = types[i];
            }

            // determine the appropriate extractor
            Class<? extends ArgumentExtractor> extractorType;
            if (FileItem.class == objectType) {
                extractorType = FileItemExtractor.class;
            } else {
                extractorType = ControllerUtils.getArgumentExtractor(parameter);
            }

            // instantiate the extractor
            extractors[i] = injector.getInstance(extractorType);

            // configure the extractor
            if (extractors[i] instanceof ConfigurableExtractor<?>) {
                ConfigurableExtractor extractor = (ConfigurableExtractor) extractors[i];
                Annotation annotation = ClassUtils.getAnnotation(parameter, extractor.getAnnotationClass());
                if (annotation != null) {
                    extractor.configure(annotation);
                }
            }

            if (extractors[i] instanceof SuffixExtractor) {
                // the last parameter can be assigned content type suffixes
                SuffixExtractor extractor = (SuffixExtractor) extractors[i];
                extractor.setSuffixes(contentTypeSuffixes);
            }

            if (collectionType != null) {
                if (extractors[i] instanceof CollectionExtractor) {
                    CollectionExtractor extractor = (CollectionExtractor) extractors[i];
                    extractor.setCollectionType(collectionType);
                } else {
                    // TODO
                    throw new PippoRuntimeException(
                            "Controller method '{}' parameter {} of type '{}' does not specify an argument extractor that supports collections!",
                            LangUtils.toString(method), i + 1, /*LangUtils.toString(collectionType, objectType)*/ objectType);
                }
            }

            if (extractors[i] instanceof TypedExtractor) {
                TypedExtractor extractor = (TypedExtractor) extractors[i];
                extractor.setObjectType(objectType);
            }

            if (extractors[i] instanceof NamedExtractor) {
                // ensure that the extractor has a proper name
                NamedExtractor namedExtractor = (NamedExtractor) extractors[i];
                if (StringUtils.isNullOrEmpty(namedExtractor.getName())) {
                    // parameter is not named via annotation
                    // try looking for the parameter name in the compiled .class file
                    if (parameter.isNamePresent()) {
                        namedExtractor.setName(parameter.getName());
                    } else {
                        log.error("Properly annotate your controller methods OR specify the '-parameters' flag for your Java compiler!");
                        throw new PippoRuntimeException(
                                "Controller method '{}' parameter {} of type '{}' does not specify a name!",
                                LangUtils.toString(method), i + 1, Util.toString(collectionType, objectType));
                    }
                }
            }
        }

    }

    /**
     * Validates that the declared consumes can actually be processed by Fathom.
     *
     * @param fathomContentTypes
     */
    protected void validateConsumes(Collection<String> fathomContentTypes) {
        Set<String> ignoreConsumes = new TreeSet<>();
        ignoreConsumes.add(Consumes.ALL);

        // these are handled by the TemplateEngine
        ignoreConsumes.add(Consumes.HTML);
        ignoreConsumes.add(Consumes.XHTML);

        // these are handled by the Servlet layer
        ignoreConsumes.add(Consumes.FORM);
        ignoreConsumes.add(Consumes.MULTIPART);

        for (String declaredConsume : declaredConsumes) {
            if (ignoreConsumes.contains(declaredConsume)) {
                continue;
            }

            String consume = declaredConsume;
            int fuzz = consume.indexOf('*');
            if (fuzz > -1) {
                // strip fuzz, we must have a registered engine for the unfuzzed content-type
                consume = consume.substring(0, fuzz);
            }

            if (!fathomContentTypes.contains(consume)) {
                if (consume.equals(declaredConsume)) {
                    throw new PippoRuntimeException("{} declares @{}(\"{}\") but there is no registered ContentTypeEngine for that type!",
                            LangUtils.toString(method), Consumes.class.getSimpleName(), declaredConsume);
                } else {
                    throw new PippoRuntimeException("{} declares @{}(\"{}\") but there is no registered ContentTypeEngine for \"{}\"!",
                            LangUtils.toString(method), Consumes.class.getSimpleName(), declaredConsume, consume);
                }
            }
        }
    }

    /**
     * Validates that the declared content-types can actually be generated by Fathom.
     *
     * @param fathomContentTypes
     */
    protected void validateProduces(Collection<String> fathomContentTypes) {
        Set<String> ignoreProduces = new TreeSet<>();
        ignoreProduces.add(Produces.TEXT);
        ignoreProduces.add(Produces.HTML);
        ignoreProduces.add(Produces.XHTML);

        for (String produces : declaredProduces) {
            if (ignoreProduces.contains(produces)) {
                continue;
            }

            if (!fathomContentTypes.contains(produces)) {
                throw new PippoRuntimeException("{} declares @{}(\"{}\") but there is no registered ContentTypeEngine for that type!",
                        LangUtils.toString(method), Produces.class.getSimpleName(), produces);
            }
        }
    }


    /**
     * Validates the declared Returns of the controller method.  If the controller method returns an object then
     * it must also declare a successful @Return with a status code in the 200 range.
     */
    protected void validateDeclaredReturns() {
        boolean returnsObject = void.class != method.getReturnType();
        if (returnsObject) {
            for (Return declaredReturn : declaredReturns) {
                if (declaredReturn.code() >= 200 && declaredReturn.code() < 300) {
                    return;
                }
            }
            throw new PippoRuntimeException("{} returns an object but does not declare a successful @{}(code=200, onResult={}.class)",
                    LangUtils.toString(method), Return.class.getSimpleName(), method.getReturnType().getSimpleName());
        }
    }

    /**
     * Determines if the incoming request is sending content this route understands.
     *
     * @param context
     * @return true if the route handles the request accept/content-type
     */
    protected boolean canConsume(Context context) {
        Set<String> contentTypes = context.getContentTypes();

        if (!declaredConsumes.isEmpty()) {
            if (declaredConsumes.contains(Consumes.ALL)) {
                log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(method), Consumes.ALL);
                return true;
            }

            Set<String> types = new LinkedHashSet<>(contentTypes);
            if (types.isEmpty()) {
                // Request does not specify a Content-Type so add Accept type(s)
                types.addAll(context.getAcceptTypes());

                // Request can handle any type, so consume the Request
                if (types.contains("*") || types.contains("*/*")) {
                    log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(method), "*/*");
                    return true;
                }
            }

            for (String type : types) {
                if (declaredConsumes.contains(type)) {
                    // explicit content-type match
                    log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(method), type);
                    return true;
                } else {
                    // look for a fuzzy content-type match
                    for (String declaredType : declaredConsumes) {
                        int fuzz = declaredType.indexOf('*');
                        if (fuzz > -1) {
                            String fuzzyType = declaredType.substring(0, fuzz);
                            if (type.startsWith(fuzzyType)) {
                                log.debug("{} will handle Request because it consumes '{}'", LangUtils.toString(method), type);
                                return true;
                            }
                        }
                    }
                }
            }

            if (types.isEmpty()) {
                log.warn("{} can not handle Request because neither 'Accept' nor 'Content-Type' are set and Route @Consumes '{}'",
                    LangUtils.toString(method), declaredConsumes);
            } else {
                log.warn("{} can not handle Request for '{}' because Route @Consumes '{}'", LangUtils.toString(method),
                        types, declaredConsumes);
            }
            return false;
        }

        return true;
    }

    protected void processRouteInterceptors(Context context) {
        if (routeInterceptors.isEmpty()) {
            return;
        }

        List<RouteMatch> chain = new ArrayList<>();
        for (RouteHandler<Context> interceptor : routeInterceptors) {
            Route route = new Route(context.getRequestMethod(), context.getRequestUri(), interceptor);
            route.setName(StringUtils.format("{}<{}>", RouteInterceptor.class.getSimpleName(),
                    route.getRouteHandler().getClass().getSimpleName()));
            RouteMatch match = new RouteMatch(route, null);
            chain.add(match);
        }
        Context subContext = new Context(context, chain);
        subContext.next();
    }

    protected Object[] prepareMethodArgs(Context context) {
        Parameter[] parameters = method.getParameters();

        if (parameters.length == 0) {
            return new Object[]{};
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            ArgumentExtractor extractor = extractors[i];
            Object value = extractor.extract(context);

            validateParameterValue(parameter, value);

            if (value == null || ClassUtils.isAssignable(value, type)) {
                args[i] = value;
            } else {
                String parameterName = ControllerUtils.getParameterName(parameter);
                throw new PippoRuntimeException("Type for '{}' is actually '{}' but was specified as '{}'!",
                        parameterName, value.getClass().getName(), type.getName());
            }
        }

        return args;
    }

    protected String getParameterName(Method method, int i) {
        Annotation annotation = getAnnotation(method, i, Param.class);
        if (annotation != null) {
            return ((Param) annotation).value();
        }

        return Optional.of(method.getParameters()[i]).filter(Parameter::isNamePresent).map(Parameter::getName).get();
    }

    protected void validateParameterValue(Parameter parameter, Object value) {
        if (value == null && parameter.isAnnotationPresent(Required.class)) {
            throw new RequiredException("'{}' is a required parameter!", ControllerUtils.getParameterName(parameter));
        }

        if (value != null && value instanceof Number) {
            Number number = (Number) value;

            if (parameter.isAnnotationPresent(Min.class)) {
                // validate required minimum value
                Min min = parameter.getAnnotation(Min.class);
                if (number.longValue() < min.value()) {
                    throw new RangeException("'{}' must be >= {}", ControllerUtils.getParameterName(parameter), min.value());
                }
            }

            if (parameter.isAnnotationPresent(Max.class)) {
                // validate required maximum value
                Max max = parameter.getAnnotation(Max.class);
                if (number.longValue() > max.value()) {
                    throw new RangeException("'{}' must be <= {}", ControllerUtils.getParameterName(parameter), max.value());
                }
            }

            if (parameter.isAnnotationPresent(Range.class)) {
                Range range = parameter.getAnnotation(Range.class);
                if (number.longValue() < range.min()) {
                    throw new RangeException("'{}' must be >= {}", ControllerUtils.getParameterName(parameter), range.min());
                }
                if (number.longValue() > range.max()) {
                    throw new RangeException("'{}' must be <= {}", ControllerUtils.getParameterName(parameter), range.max());
                }
            }
        }
    }

    protected Class<?> getParameterGenericType(Parameter parameter) {
        Type parameterType = parameter.getParameterizedType();
        if (!ParameterizedType.class.isAssignableFrom(parameterType.getClass())) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}' of '{}'",
                    ControllerUtils.getParameterName(parameter), LangUtils.toString(method));
        }

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        try {
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new PippoRuntimeException("Please specify a generic parameter type for '{}' of '{}'",
                    ControllerUtils.getParameterName(parameter), LangUtils.toString(method));
        }
    }

    /**
     * Specify Response cache controls.
     *
     * @param context
     */
    protected void specifyCacheControls(Context context) {
        if (isNoCache) {
            log.debug("NoCache detected, response may not be cached");
            context.getResponse().noCache();
        }
    }

    /**
     * Specify the Response content-type by...
     * <ol>
     * <li>setting the first Produces content type</li>
     * <li>negotiating with the Request if multiple content-types are specified in Produces</li>
     * </ol>
     *
     * @param context
     */
    protected void specifyContentType(Context context) {
        if (!declaredProduces.isEmpty()) {
            // Specify first Produces content-type
            String defaultContentType = declaredProduces.get(0);
            context.getResponse().contentType(defaultContentType);

            if (declaredProduces.size() > 1) {
                // negotiate content-type from Request Accept/Content-Type
                context.negotiateContentType();
            }
        }
    }

    protected void handleDeclaredThrownException(Exception e, Method method, Context context) {
        Class<? extends Exception> exceptionClass = e.getClass();
        for (Return declaredReturn : declaredReturns) {
            if (exceptionClass.isAssignableFrom(declaredReturn.onResult())) {
                context.status(declaredReturn.code());

                // prefer declared message to exception message
                String message = StringUtils.isNullOrEmpty(declaredReturn.description()) ? e.getMessage() : declaredReturn.description();

                if (!StringUtils.isNullOrEmpty(declaredReturn.descriptionKey())) {
                    // retrieve localized message, fallback to declared message
                    message = messages.getWithDefault(declaredReturn.descriptionKey(), message, context);
                }

                if (!StringUtils.isNullOrEmpty(message)) {
                    context.setLocal("message", message);
                }

                validateResponseHeaders(declaredReturn, context);

                log.warn("Handling declared return exception '{}' for '{}'", e.getMessage(), LangUtils.toString(method));

                return;
            }
        }

        if (e instanceof RuntimeException) {
            // pass-through the thrown exception
            throw (RuntimeException) e;
        }

        // undeclared exception, wrap & throw
        throw new PippoRuntimeException(e);
    }

    protected void validateResponseHeaders(Return aReturn, Context context) {
        for (Class<? extends ReturnHeader> returnHeader : aReturn.headers()) {
            ReturnHeader header = ClassUtils.newInstance(returnHeader);
            String name = header.getHeaderName();
            String defaultValue = header.getDefaultValue();
            // FIXME need to expose getHeader in Pippo Response
            String value = null; //context.getHeader(name);

            if (value == null) {
                if (StringUtils.isNullOrEmpty(defaultValue)) {
                    log.warn("No value specified for the declared response header '{}'", name);
                } else {
                    context.setHeader(name, defaultValue);
                    log.debug("No value specified for the declared response header '{}', defaulting to '{}'", name, defaultValue);
                }
            }

        }
    }

}
