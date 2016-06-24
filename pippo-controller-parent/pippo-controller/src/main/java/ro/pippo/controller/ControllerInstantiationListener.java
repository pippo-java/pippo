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
package ro.pippo.controller;

/**
 * Listener interface that receives messages when controllers are constructed.
 * It be used to implement things like dependency injection support etc.
 *
 * @author Decebal Suiu
 */
public interface ControllerInstantiationListener {

    /**
     * Called for every controller that is instantiated.
     *
     * @param controller
     */
    void onInstantiation(Controller controller);

}
