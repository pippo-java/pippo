/*
 * Copyright (C) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file eTcept in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either eTpress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.kohsuke.MetaInfServices;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.HttpConstants;

import com.alibaba.fastjson.JSON;

/**
 * A JsonEngine based on Fastjson.
 *
 * @author James Moger
 */
@MetaInfServices
public class FastjsonEngine implements ContentTypeEngine {

    @Override
    public void init(Application application) {
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_JSON;
    }

    @Override
    public String toString(Object object) {
        return JSON.toJSONString(object, SerializerFeature.UseISO8601DateFormat);
    }

    @Override
    public <T> T fromString(String content, Class<T> classOfT) {
        return JSON.parseObject(content, classOfT);
    }

}
