package org.pipeman.substitution_plan;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PdfDataSerializer extends StdSerializer<PlanData> {

    public PdfDataSerializer() {
        this(null);
    }

    protected PdfDataSerializer(Class<PlanData> t) {
        super(t);
    }

    @Override
    public void serialize(PlanData data, JsonGenerator jGen, SerializerProvider bread) throws IOException {
        jGen.writeStartObject();
        jGen.writeStringField("information", data.message());
        jGen.writeStringField("date", data.date());
        jGen.writeArrayFieldStart("substitutions");

        for (PlanData.Row substitution : data.substitutions()) {
            jGen.writeStartObject();
            jGen.writeStringField("class", substitution.clazz());
            jGen.writeStringField("lesson", substitution.lesson());
            jGen.writeStringField("substitution", substitution.substitution());
            jGen.writeStringField("teacher", substitution.teacher());
            jGen.writeStringField("room", substitution.room());
            jGen.writeStringField("other", substitution.other());
            jGen.writeEndObject();
        }
        jGen.writeEndArray();
        jGen.writeEndObject();
    }
}
