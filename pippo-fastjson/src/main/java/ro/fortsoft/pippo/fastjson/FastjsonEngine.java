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
package ro.fortsoft.pippo.fastjson;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.HttpConstants;
import ro.fortsoft.pippo.core.RepresentationEngine;

import com.alibaba.fastjson.JSON;

/**
 * A JsonEngine based on Fastjson.
 *
 * @author James Moger
 */
public class FastjsonEngine implements RepresentationEngine {

	@Override
	public void init(Application application) {
	}

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_JSON;
    }

	@Override
	public String toRepresentation(Object object) {
		return JSON.toJSONString(object);
	}

	@Override
	public <T> T fromRepresentation(String json, Class<T> classOfT) {
		return JSON.parseObject(json, classOfT);
	}

}
