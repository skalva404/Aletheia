package com.outbrain.aletheia.kafka.serialization;

import com.outbrain.aletheia.datum.envelope.AvroDatumEnvelopeSerDe;
import com.outbrain.aletheia.datum.envelope.avro.DatumEnvelope;
import com.outbrain.aletheia.datum.serialization.DatumSerDe;
import com.outbrain.aletheia.datum.serialization.DatumTypeVersion;
import com.outbrain.aletheia.datum.serialization.SerializedDatum;

import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Implementation of a Kafka Deserializer for deserializing a datum.
 *
 * @param <TDomainClass> The type of Datum.
 */
public class AletheiaKafkaDeserializer<TDomainClass> implements Deserializer<TDomainClass> {

  private final AvroDatumEnvelopeSerDe avroDatumEnvelopeSerDe = new AvroDatumEnvelopeSerDe();
  private final DatumSerDe<TDomainClass> datumSerDe;
  private final SerDeListener<TDomainClass> listener;

  public AletheiaKafkaDeserializer(final DatumSerDe<TDomainClass> datumSerDe) {
    this(datumSerDe, null);
  }

  @SuppressWarnings("unchecked")
  public AletheiaKafkaDeserializer(DatumSerDe<TDomainClass> datumSerDe, SerDeListener<TDomainClass> listener) {
    this.datumSerDe = datumSerDe;
    this.listener = (listener != null) ? listener : SerDeListener.EMPTY;
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {

  }

  @Override
  public TDomainClass deserialize(String topic, byte[] data) {
    final DatumEnvelope datumEnvelope = avroDatumEnvelopeSerDe.deserializeDatumEnvelope(ByteBuffer.wrap(data));
    final DatumTypeVersion datumTypeVersion = new DatumTypeVersion(datumEnvelope.getDatumTypeId().toString(), datumEnvelope.getDatumSchemaVersion());
    final TDomainClass datum = datumSerDe.deserializeDatum(new SerializedDatum(datumEnvelope.getDatumBytes(), datumTypeVersion));
    listener.onDeserialize(topic, datum, datumEnvelope, data.length);
    return datum;
  }

  @Override
  public void close() {

  }
}
