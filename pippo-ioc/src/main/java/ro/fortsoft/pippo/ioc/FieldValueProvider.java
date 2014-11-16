/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.ioc;

import java.lang.reflect.Field;

/**
 * Provider used by injector to generate values for fields of the object being injected.
 *
 * @author Decebal Suiu
 */
public interface FieldValueProvider {

    public Object getFieldValue(Field field, Object fieldOwner);

    /**
     * Returns true if the provifrt can generate a value for the field, false otherwise.
     * If this method returns false, getFieldValue() will not be called on this provider.
     */
    public boolean supportsField(Field field);

}
