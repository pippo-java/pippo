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
package ro.pippo.demo.controller;

import ro.pippo.controller.Body;
import ro.pippo.controller.Controller;
import ro.pippo.core.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author James Moger
 */
public class CollectionsController extends Controller {

    public void index() {
        getResponse()
            .bind("mySet", Arrays.asList(2, 2, 4, 4, 6, 6, 8, 8))
            .bind("myList", Arrays.asList(1945, 1955, 1965, 1975, 1985, 1995, 2005, 2015))
            .bind("myTreeSet", Arrays.asList("Blue", "Orange", "Blue", "Orange", "Red", "Green", "Red", "Green"))
            .render("collections");
    }

    public void update(@Param("mySet") Set<Integer> mySet,
                       @Param("myList") List<Integer> myList,
                       @Param("myTreeSet") TreeSet<String> treeSet) {

        StringBuilder sb = new StringBuilder();
        sb.append("mySet=").append(mySet.toString()).append('\n');
        sb.append("myList=").append(myList.toString()).append('\n');
        sb.append("myTreeSet=").append(treeSet.toString()).append('\n');
        getResponse().text().send(sb.toString());
    }

    public void post(@Body List<String> desserts) {
        StringBuilder sb = new StringBuilder();
        sb.append("desserts=").append(desserts.toString()).append('\n');
        getResponse().text().send(sb.toString());
    }

}
