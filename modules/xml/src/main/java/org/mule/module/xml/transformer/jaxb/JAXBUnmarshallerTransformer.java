/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.transformer.jaxb;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transformer.AbstractTransformer;
import org.mule.transformer.types.DataTypeFactory;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.w3c.dom.Node;

/**
 * Allows un-marshaling of XML generated by JAXB to a Java object graph. By default the returnType for this transformer is {@link Object}
 * If a specific returnType is set and ff no external {@link javax.xml.bind.JAXBContext} is set on the transformer, then a
 * {@link javax.xml.bind.JAXBContext} will be created using the returnType.
 *
 * @since 3.0
 */
public class JAXBUnmarshallerTransformer extends AbstractTransformer
{
    protected JAXBContext jaxbContext;

    public JAXBUnmarshallerTransformer()
    {
        registerSourceType(DataTypeFactory.STRING);
        registerSourceType(DataTypeFactory.create(Writer.class));
        registerSourceType(DataTypeFactory.create(File.class));
        registerSourceType(DataTypeFactory.create(URL.class));
        registerSourceType(DataTypeFactory.create(Node.class));
        registerSourceType(DataTypeFactory.INPUT_STREAM);
        registerSourceType(DataTypeFactory.create(Source.class));
        registerSourceType(DataTypeFactory.create(XMLStreamReader.class));
        registerSourceType(DataTypeFactory.create(XMLEventReader.class));
    }

    public JAXBUnmarshallerTransformer(JAXBContext jaxbContext, DataType returnType)
    {
        this();
        this.jaxbContext = jaxbContext;
        setReturnDataType(returnType);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        super.initialise();
        if (jaxbContext == null)
        {
            if(Object.class.equals(getReturnDataType().getType()))
            {
                throw new InitialisationException(CoreMessages.objectIsNull("jaxbContext"), this);
            }
            else
            {
                try
                {
                    jaxbContext = JAXBContext.newInstance(getReturnDataType().getType());
                }
                catch (JAXBException e)
                {
                    throw new InitialisationException(e, this);
                }
            }
        }
    }

    @Override
    protected Object doTransform(Object src, String encoding) throws TransformerException
    {
        try
        {
            final Unmarshaller u = jaxbContext.createUnmarshaller();
            Object result = null;
            if (src instanceof String)
            {
                result = u.unmarshal(new StringReader((String) src));
            }
            else if (src instanceof File)
            {
                result = u.unmarshal((File) src);
            }
            else if (src instanceof URL)
            {
                result = u.unmarshal((URL) src);
            }
            else if (src instanceof InputStream)
            {
                result = u.unmarshal((InputStream) src);
            }
            else if (src instanceof Node)
            {
                result = u.unmarshal((Node) src, getReturnClass());
            }
            else if (src instanceof Source)
            {
                result = u.unmarshal((Source) src, getReturnClass());
            }
            else if (src instanceof XMLStreamReader)
            {
                result = u.unmarshal((XMLStreamReader) src, getReturnClass());
            }
            else if (src instanceof XMLEventReader)
            {
                result = u.unmarshal((XMLEventReader) src, getReturnClass());
            }
            if (result != null)
            {
                // If we get a JAXB element, return its contents
                if (result instanceof JAXBElement)
                {
                    result = ((JAXBElement)result).getValue();
                }
            }
            return result;
        }
        catch (Exception e)
        {
            throw new TransformerException(this, e);
        }
    }

    public JAXBContext getJaxbContext()
    {
        return jaxbContext;
    }

    public void setJaxbContext(JAXBContext jaxbContext)
    {
        this.jaxbContext = jaxbContext;
    }
}
