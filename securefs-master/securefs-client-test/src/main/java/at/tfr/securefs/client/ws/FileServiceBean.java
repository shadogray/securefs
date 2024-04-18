
package at.tfr.securefs.client.ws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebService(name = "FileServiceBean", targetNamespace = "http://securefs.tfr.at/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface FileServiceBean {


    /**
     * 
     * @param relativePath
     * @return
     *     returns byte[]
     * @throws IOException_Exception
     */
    @WebMethod
    @WebResult(name = "bytes", targetNamespace = "")
    @RequestWrapper(localName = "read", targetNamespace = "http://securefs.tfr.at/", className = "at.tfr.securefs.client.ws.Read")
    @ResponseWrapper(localName = "readResponse", targetNamespace = "http://securefs.tfr.at/", className = "at.tfr.securefs.client.ws.ReadResponse")
    public byte[] read(
        @WebParam(name = "relativePath", targetNamespace = "")
        String relativePath)
        throws IOException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns boolean
     * @throws IOException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "delete", targetNamespace = "http://securefs.tfr.at/", className = "at.tfr.securefs.client.ws.Delete")
    @ResponseWrapper(localName = "deleteResponse", targetNamespace = "http://securefs.tfr.at/", className = "at.tfr.securefs.client.ws.DeleteResponse")
    public boolean delete(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws IOException_Exception
    ;

    /**
     * 
     * @param relativePath
     * @param bytes
     * @throws IOException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "write", targetNamespace = "http://securefs.tfr.at/", className = "at.tfr.securefs.client.ws.Write")
    @ResponseWrapper(localName = "writeResponse", targetNamespace = "http://securefs.tfr.at/", className = "at.tfr.securefs.client.ws.WriteResponse")
    public void write(
        @WebParam(name = "relativePath", targetNamespace = "")
        String relativePath,
        @WebParam(name = "bytes", targetNamespace = "")
        byte[] bytes)
        throws IOException_Exception
    ;

}
