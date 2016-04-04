/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.pebble;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.escaper.SafeString;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;

public class PrettyTimeExtension extends AbstractExtension {

    private final LoadingCache<Locale, PrettyTime> prettyTimeCache;

    public PrettyTimeExtension() {
        this.prettyTimeCache = CacheBuilder.newBuilder().maximumSize(10).build(new CacheLoader<Locale, PrettyTime>() {
            @Override
            public PrettyTime load(Locale locale) throws Exception {
                return new PrettyTime(locale);
            }
        });
    }

    @Override
    public Map<String, Filter> getFilters() {
        Map<String, Filter> filters = new HashMap<>();
        filters.put("prettyTime", new PrettyTimeFilter());
        return filters;
    }

    public class PrettyTimeFilter implements Filter {

        @Override
        public List<String> getArgumentNames() {
            return Collections.emptyList();
        }

        @Override
        public Object apply(Object input, Map<String, Object> args) {
            if (input == null) {
                return null;
            }

            EvaluationContext context = (EvaluationContext) args.get("_context");
            Locale locale = context.getLocale();

            String result = prettyTimeCache.getUnchecked(locale).format(getFormattableObject(input));

            return new SafeString(result);
        }

        private Date getFormattableObject(Object value) {

            if (value instanceof Date) {
                return (Date) value;
            } else if (value instanceof Calendar) {
                return ((Calendar) value).getTime();
            } else if (value instanceof Long) {
                return new Date((Long) value);
            } else {
                throw new RuntimeException("Formattable object for PrettyTime not found!");
            }
        }
    }
}
