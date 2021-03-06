package org.corfudb.protocols.wireprotocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.ParameterizedType;

/**
 * A message type which represents an encapsulated Corfu
 * payload.
 *
 * T should be either of a primitive boxed type (Long, Integer, etc)
 * or of ICorfuPayload.
 *
 * NEVER, EVER use this class as a raw type. This class DEPENDS on
 * the generic captured at runtime by CorfuMsg (via TypeToken).
 *
 * Created by mwei on 8/1/16.
 */
@NoArgsConstructor
public class CorfuPayloadMsg<T> extends CorfuMsg {

    /**
     * The payload.
     */
    @Getter
    private T payload;


    public CorfuPayloadMsg(CorfuMsgType msgType, T payload) {
        super(msgType);
        this.payload = payload;
    }

    /**
     * Serialize the message into the given bytebuffer.
     *
     * @param buffer The buffer to serialize to.
     */
    @Override
    public void serialize(ByteBuf buffer) {
        super.serialize(buffer);
        ICorfuPayload.serialize(buffer, payload);
    }

    /**
     * Parse the rest of the message from the buffer. Classes that extend CorfuMsg
     * should parse their fields in this method.
     *
     * @param buffer
     */
    @Override
    @SuppressWarnings("unchecked")
    public void fromBuffer(ByteBuf buffer) {
        payload = (T) ICorfuPayload.fromBuffer(buffer,
                (Class)((ParameterizedType)msgType.messageType.getType())
                        .getActualTypeArguments()[0]);
    }
}
