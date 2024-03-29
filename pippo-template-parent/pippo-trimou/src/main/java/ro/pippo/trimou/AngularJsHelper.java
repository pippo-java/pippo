/*
 * Copyright (C) 2014-present the original author or authors.
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
package ro.pippo.trimou;

import org.trimou.handlebars.BasicValueHelper;
import org.trimou.handlebars.Options;

import com.google.common.base.Joiner;

/**
 * <p>
 * AngularJS helper allows you to use the double-brace markup for both Trimou
 * and AngularJS.
 * </p>
 * <code>
 * {{ng foo bar | filter | whatever}}
 * </code>
 *
 * @author James Moger
 *
 */
public class AngularJsHelper extends BasicValueHelper {

    @Override
    public void execute(Options options) {
        options.append("{{");
        String args = Joiner.on(' ').join(options.getParameters());
        options.append(args);
        options.append("}}");
    }
}
