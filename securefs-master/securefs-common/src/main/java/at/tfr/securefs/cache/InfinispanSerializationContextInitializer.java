package at.tfr.securefs.cache;

import at.tfr.securefs.data.ProcessFilesData;
import at.tfr.securefs.data.ValidationData;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

// uncomment to generate Protostream files:
@AutoProtoSchemaBuilder(includeClasses = {ClusterState.class, ValidationData.class, ProcessFilesData.class}, autoImportClasses = true)
public interface InfinispanSerializationContextInitializer extends SerializationContextInitializer {
}
