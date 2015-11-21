
package at.tfr.securefs.client.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the at.tfr.securefs package.
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

    private final static QName _ReadResponse_QNAME = new QName("http://securefs.tfr.at/", "readResponse");
    private final static QName _Read_QNAME = new QName("http://securefs.tfr.at/", "read");
    private final static QName _WriteResponse_QNAME = new QName("http://securefs.tfr.at/", "writeResponse");
    private final static QName _IOException_QNAME = new QName("http://securefs.tfr.at/", "IOException");
    private final static QName _Write_QNAME = new QName("http://securefs.tfr.at/", "write");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: at.tfr.securefs
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReadResponse }
     *
     */
    public ReadResponse createReadResponse() {
        return new ReadResponse();
    }

    /**
     * Create an instance of {@link Read }
     *
     */
    public Read createRead() {
        return new Read();
    }

    /**
     * Create an instance of {@link WriteResponse }
     *
     */
    public WriteResponse createWriteResponse() {
        return new WriteResponse();
    }

    /**
     * Create an instance of {@link IOException }
     *
     */
    public IOException createIOException() {
        return new IOException();
    }

    /**
     * Create an instance of {@link Write }
     *
     */
    public Write createWrite() {
        return new Write();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReadResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://securefs.tfr.at/", name = "readResponse")
    public JAXBElement<ReadResponse> createReadResponse(ReadResponse value) {
        return new JAXBElement<ReadResponse>(_ReadResponse_QNAME, ReadResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Read }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://securefs.tfr.at/", name = "read")
    public JAXBElement<Read> createRead(Read value) {
        return new JAXBElement<Read>(_Read_QNAME, Read.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WriteResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://securefs.tfr.at/", name = "writeResponse")
    public JAXBElement<WriteResponse> createWriteResponse(WriteResponse value) {
        return new JAXBElement<WriteResponse>(_WriteResponse_QNAME, WriteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IOException }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://securefs.tfr.at/", name = "IOException")
    public JAXBElement<IOException> createIOException(IOException value) {
        return new JAXBElement<IOException>(_IOException_QNAME, IOException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Write }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://securefs.tfr.at/", name = "write")
    public JAXBElement<Write> createWrite(Write value) {
        return new JAXBElement<Write>(_Write_QNAME, Write.class, null, value);
    }

}
