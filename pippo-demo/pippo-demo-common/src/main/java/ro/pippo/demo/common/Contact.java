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
package ro.pippo.demo.common;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Decebal Suiu
 */
@XmlRootElement
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String phone;
    private String address;

    public Contact() {
    }

    public Contact(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @XmlAttribute
    public Contact setId(int id) {
        this.id = id;

        return this;
    }

    public String getName() {
        return name;
    }

    @XmlElement
    public Contact setName(String name) {
        this.name = name;

        return this;
    }

    public String getPhone() {
        return phone;
    }

    @XmlElement
    public Contact setPhone(String phone) {
        this.phone = phone;

        return this;
    }

    public String getAddress() {
        return address;
    }

    @XmlElement
    public Contact setAddress(String address) {
        this.address = address;

        return this;
    }

    @Override
    public String toString() {
        return "Contact {" +
                "name='" + name + '\'' +
                ", phone=" + phone +
                ", address='" + address + '\'' +
                '}';
    }

}
