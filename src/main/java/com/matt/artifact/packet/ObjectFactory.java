//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.20 at 01:17:29 PM CEST 
//


package com.matt.artifact.packet;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.matt.artifact package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.matt.artifact
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ImagesType }
     * 
     */
    public ImagesType createImagesType() {
        return new ImagesType();
    }

    /**
     * Create an instance of {@link Packets }
     * 
     */
    public Packets createPackets() {
        return new Packets();
    }

    /**
     * Create an instance of {@link Packet }
     * 
     */
    public Packet createPacket() {
        return new Packet();
    }

    /**
     * Create an instance of {@link ImagesType.Image }
     * 
     */
    public ImagesType.Image createImagesTypeImage() {
        return new ImagesType.Image();
    }

}
