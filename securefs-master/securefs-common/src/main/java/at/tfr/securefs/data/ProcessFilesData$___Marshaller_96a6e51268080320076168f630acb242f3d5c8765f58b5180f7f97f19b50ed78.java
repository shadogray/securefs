/*
 Generated by org.infinispan.protostream.annotations.impl.processor.MarshallerSourceCodeGenerator
 for class at.tfr.securefs.data.ProcessFilesData
*/

package at.tfr.securefs.data;

import at.tfr.securefs.data.ProcessFilesData;

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
public final class ProcessFilesData$___Marshaller_96a6e51268080320076168f630acb242f3d5c8765f58b5180f7f97f19b50ed78 extends org.infinispan.protostream.annotations.impl.GeneratedMarshallerBase implements org.infinispan.protostream.ProtobufTagMarshaller<at.tfr.securefs.data.ProcessFilesData> {

   private org.infinispan.protostream.impl.BaseMarshallerDelegate __md$6;
   
   private org.infinispan.protostream.impl.BaseMarshallerDelegate __md$12;
   
   @Override
   public Class<at.tfr.securefs.data.ProcessFilesData> getJavaClass() { return at.tfr.securefs.data.ProcessFilesData.class; }
   
   @Override
   public String getTypeName() { return "ProcessFilesData"; }
   
   @Override
   public at.tfr.securefs.data.ProcessFilesData read(org.infinispan.protostream.ProtobufTagMarshaller.ReadContext $1) throws java.io.IOException {
      final org.infinispan.protostream.TagReader $in = $1.getReader();
      final at.tfr.securefs.data.ProcessFilesData o = new at.tfr.securefs.data.ProcessFilesData();
      long __bits$0 = 0;
      java.util.ArrayList __c$12 = new java.util.ArrayList();
      boolean done = false;
      while (!done) {
         final int tag = $in.readTag();
         switch (tag) {
            case 0: {
               done = true;
               break;
            }
            case (1 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$1 = $in.readString();
               o.node = __v$1;
               break;
            }
            case (2 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$2 = $in.readString();
               o.principal = __v$2;
               break;
            }
            case (3 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_VARINT): {
               boolean __v$3 = $in.readBool();
               o.processActive = __v$3;
               __bits$0 |= 1L;
               break;
            }
            case (4 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_VARINT): {
               boolean __v$4 = $in.readBool();
               o.update = __v$4;
               __bits$0 |= 2L;
               break;
            }
            case (5 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_VARINT): {
               boolean __v$5 = $in.readBool();
               o.allowOverwriteExisting = __v$5;
               __bits$0 |= 4L;
               break;
            }
            case (6 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               if (__md$6 == null) __md$6 = ((org.infinispan.protostream.impl.SerializationContextImpl) $1.getSerializationContext()).getMarshallerDelegate(at.tfr.securefs.data.ValidationData.class);
               int length = $in.readUInt32();
               int oldLimit = $in.pushLimit(length);
               at.tfr.securefs.data.ValidationData __v$6 = (at.tfr.securefs.data.ValidationData) readMessage(__md$6, $1);
               $in.checkLastTagWas(0);
               $in.popLimit(oldLimit);
               o.validationData = __v$6;
               break;
            }
            case (7 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$7 = $in.readString();
               o.fromRootPath = __v$7;
               break;
            }
            case (8 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$8 = $in.readString();
               o.toRootPath = __v$8;
               break;
            }
            case (9 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$9 = $in.readString();
               o.currentFromPath = __v$9;
               break;
            }
            case (10 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$10 = $in.readString();
               o.currentToPath = __v$10;
               break;
            }
            case (11 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               java.lang.String __v$11 = $in.readString();
               o.lastError = __v$11;
               break;
            }
            case (12 << org.infinispan.protostream.descriptors.WireType.TAG_TYPE_NUM_BITS | org.infinispan.protostream.descriptors.WireType.WIRETYPE_LENGTH_DELIMITED): {
               if (__md$12 == null) __md$12 = ((org.infinispan.protostream.impl.SerializationContextImpl) $1.getSerializationContext()).getMarshallerDelegate(at.tfr.securefs.cache.StateInfo.class);
               int length = $in.readUInt32();
               int oldLimit = $in.pushLimit(length);
               at.tfr.securefs.cache.StateInfo __v$12 = (at.tfr.securefs.cache.StateInfo) readMessage(__md$12, $1);
               $in.checkLastTagWas(0);
               $in.popLimit(oldLimit);
               __c$12.add(__v$12);
               break;
            }
            default: {
               if (!$in.skipField(tag)) done = true;
            }
         }
      }
      o.errors = __c$12;
      
      if ((__bits$0 & 7L) != 7L) {
         final StringBuilder missing = new StringBuilder();
         if ((__bits$0 & 1L) == 0) {
            missing.append("processActive");
         }
         if ((__bits$0 & 2L) == 0) {
            if (missing.length() > 0) missing.append(", ");
            missing.append("update");
         }
         if ((__bits$0 & 4L) == 0) {
            if (missing.length() > 0) missing.append(", ");
            missing.append("allowOverwriteExisting");
         }
         throw new java.io.IOException("Required field(s) missing from input stream : " + missing);
      }
      return o;
   }
   
   @Override
   public void write(org.infinispan.protostream.ProtobufTagMarshaller.WriteContext $1, at.tfr.securefs.data.ProcessFilesData $2) throws java.io.IOException {
      final org.infinispan.protostream.TagWriter $out = $1.getWriter();
      final at.tfr.securefs.data.ProcessFilesData o = (at.tfr.securefs.data.ProcessFilesData) $2;
      {
         final java.lang.String __v$1 = o.node;
         if (__v$1 != null) $out.writeString(1, __v$1);
      }
      {
         final java.lang.String __v$2 = o.principal;
         if (__v$2 != null) $out.writeString(2, __v$2);
      }
      {
         final boolean __v$3 = o.processActive;
         $out.writeBool(3, __v$3);
      }
      {
         final boolean __v$4 = o.update;
         $out.writeBool(4, __v$4);
      }
      {
         final boolean __v$5 = o.allowOverwriteExisting;
         $out.writeBool(5, __v$5);
      }
      {
         final at.tfr.securefs.data.ValidationData __v$6 = (at.tfr.securefs.data.ValidationData) o.validationData;
         if (__v$6 != null) {
            if (__md$6 == null) __md$6 = ((org.infinispan.protostream.impl.SerializationContextImpl) $1.getSerializationContext()).getMarshallerDelegate(at.tfr.securefs.data.ValidationData.class);
            writeNestedMessage(__md$6, $1, 6, __v$6);
         }
      }
      {
         final java.lang.String __v$7 = o.fromRootPath;
         if (__v$7 != null) $out.writeString(7, __v$7);
      }
      {
         final java.lang.String __v$8 = o.toRootPath;
         if (__v$8 != null) $out.writeString(8, __v$8);
      }
      {
         final java.lang.String __v$9 = o.currentFromPath;
         if (__v$9 != null) $out.writeString(9, __v$9);
      }
      {
         final java.lang.String __v$10 = o.currentToPath;
         if (__v$10 != null) $out.writeString(10, __v$10);
      }
      {
         final java.lang.String __v$11 = o.lastError;
         if (__v$11 != null) $out.writeString(11, __v$11);
      }
      {
         final java.util.Collection __c$12 = o.errors;
         if (__c$12 != null) 
            for (java.util.Iterator it = __c$12.iterator(); it.hasNext(); ) {
               final at.tfr.securefs.cache.StateInfo __v$12 = (at.tfr.securefs.cache.StateInfo) it.next();
               {
                  if (__md$12 == null) __md$12 = ((org.infinispan.protostream.impl.SerializationContextImpl) $1.getSerializationContext()).getMarshallerDelegate(at.tfr.securefs.cache.StateInfo.class);
                  writeNestedMessage(__md$12, $1, 12, __v$12);
               }
            }
      }
   }
}