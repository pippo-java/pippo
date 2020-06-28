package ro.pippo.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PERSON")
public class Person {

    @XmlElement(name = "NAME")
    public String name;

}
