package com.example.messagemod.network;

import com.example.messagemod.database.DatabaseManager;
import com.example.messagemod.database.repository.MessageRepository;
import com.example.messagemod.proto.MessageProto;
import com.google.protobuf.InvalidProtocolBufferException;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);

    public static final Identifier MESSAGE_PACKET_ID = Identifier.of("messagemod", "message");

    public static void registerServerReceivers() {
        LOGGER.info("Регистрация обработчиков сетевых пакетов...");

        PayloadTypeRegistry.playC2S().register(MessagePayload.ID, MessagePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MessagePayload.ID, (payload, context) -> {
            Objects.requireNonNull(context.player().getServer()).execute(() -> {
                try {
                    MessageProto.Message protoMessage = MessageProto.Message.parseFrom(payload.data());
                    String text = protoMessage.getText();

                    LOGGER.info("Получено сообщение от игрока {}: {}",
                            context.player().getName().getString(), text);

                    if (text.length() > 256) {
                        LOGGER.warn("Сообщение слишком длинное ({}), обрезаем до 256 символов", text.length());
                        text = text.substring(0, 256);
                    }

                    if (DatabaseManager.getInstance().isInitialized()) {
                        MessageRepository repository = new MessageRepository(
                                DatabaseManager.getInstance().getSessionFactory()
                        );

                        repository.save(context.player().getUuid(), text);

                        context.player().sendMessage(
                                net.minecraft.text.Text.literal("§aСообщение успешно сохранено в базе данных!")
                        );
                    } else {
                        LOGGER.error("База данных не инициализирована!");
                        context.player().sendMessage(
                                net.minecraft.text.Text.literal("§cОшибка: база данных недоступна")
                        );
                    }

                } catch (InvalidProtocolBufferException e) {
                    LOGGER.error("Ошибка декодирования Protobuf сообщения", e);
                    context.player().sendMessage(
                            net.minecraft.text.Text.literal("§cОшибка при обработке сообщения")
                    );
                }
            });
        });

        LOGGER.info("Обработчики сетевых пакетов зарегистрированы!");
    }

    public record MessagePayload(byte[] data) implements CustomPayload {
        public static final CustomPayload.Id<MessagePayload> ID = new CustomPayload.Id<>(MESSAGE_PACKET_ID);

        public static final PacketCodec<RegistryByteBuf, MessagePayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    buf.writeVarInt(value.data.length);
                    buf.writeBytes(value.data);
                },
                (buf) -> {
                    int length = buf.readVarInt();
                    byte[] data = new byte[length];
                    buf.readBytes(data);
                    return new MessagePayload(data);
                }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
