/*
 Generated by org.infinispan.protostream.annotations.impl.processor.MarshallerSourceCodeGenerator
 for class at.tfr.securefs.cache.StateInfo
*/

package at.tfr.securefs.cache;

/**
 * WARNING: Generated code! Do not edit!
 *
 * @private
 */
@javax.annotation.processing.Generated(
   value = "org.infinispan.protostream.annotations.impl.processor.AutoProtoSchemaBuilderAnnotationProcessor",
   comments = "Please do not edit this file!"
)
@SuppressWarnings("all")
public final class StateInfo$___Marshaller_86c7f0c94d5cfbeddaf3f0741b4ba640c77a6eea04accec8254fbe929833bb1b extends org.infinispan.protostream.annotations.impl.GeneratedMarshallerBase implements org.infinispan.protostream.ProtobufTagMarshaller<StateInfo> {

   @Override
   public Class<StateInfo> getJavaClass() { return StateInfo.class; }
   
   @Override
   public String getTypeName() { return "StateInfo"; }
   
   @Override
   public StateInfo read(ReadContext $1) throws java.io.IOException {
      final org.infinispan.protostream.TagReader $in = $1.getReader();
      String __v$1 = null;
      String __v$2 = null;
      boolean done = false;
      while (!done) {
         final int tag = $in.readTag();
         switch (tag) {
            case 0: {
               done = true;
               break;
            }
            case (1 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               __v$1 = $in.readString();
               break;
            }
            case (2 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               __v$2 = $in.readString();
               break;
            }
            default: {
               if (!$in.skipField(tag)) done = true;
            }
         }
      }
      return new StateInfo(__v$1, __v$2);
   }
   
   @Override
   public void write(WriteContext $1, StateInfo $2) throws java.io.IOException {
      final org.infinispan.protostream.TagWriter $out = $1.getWriter();
      final StateInfo o = (StateInfo) $2;
      {
         final String __v$1 = o.host;
         if (__v$1 != null) $out.writeString(1, __v$1);
      }
      {
         final String __v$2 = o.info;
         if (__v$2 != null) $out.writeString(2, __v$2);
      }
   }
}
