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

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.escaper.SafeString;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrettyTimeExtension extends AbstractExtension {

    private final Map<Locale, PrettyTime> prettyTimeCache;

    public PrettyTimeExtension() {
        prettyTimeCache = Collections.synchronizedMap(new LRUHashMap<>(10));
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
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
            if (input == null) {
                return null;
            }

            Locale locale = context.getLocale();

            String result = getPrettyTime(locale).format(getFormattableObject(input));

            return new SafeString(result);
        }

        private PrettyTime getPrettyTime(Locale locale) {
            return prettyTimeCache.computeIfAbsent(locale, PrettyTime::new);
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

    class LRUHashMap<K, V> extends LinkedHashMap<K, V> {

        private int cacheSize;

        public LRUHashMap(int cacheSize) {
            super(cacheSize + 1, 1.0f, true);

            this.cacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() >= cacheSize;
        }

    }

}
