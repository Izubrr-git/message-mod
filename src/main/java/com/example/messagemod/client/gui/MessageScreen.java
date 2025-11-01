package com.example.messagemod.client.gui;

import com.example.messagemod.network.NetworkHandler;
import com.example.messagemod.proto.MessageProto;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageScreen.class);

    private TextFieldWidget textField;
    private ButtonWidget sendButton;
    private final Screen parent;

    public MessageScreen(Screen parent) {
        super(Text.literal("Отправить сообщение"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        textField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 150,
                this.height / 2 - 10,
                300,
                20,
                Text.literal("Введите сообщение...")
        );

        textField.setMaxLength(256);
        textField.setPlaceholder(Text.literal("Введите текст сообщения..."));
        textField.setFocused(true);

        this.addSelectableChild(textField);

        sendButton = ButtonWidget.builder(
                        Text.literal("Отправить"),
                        button -> onSendButtonPressed()
                )
                .dimensions(
                        this.width / 2 - 75,
                        this.height / 2 + 25,
                        150,
                        20
                )
                .build();

        this.addDrawableChild(sendButton);

        ButtonWidget cancelButton = ButtonWidget.builder(
                        Text.literal("Отмена"),
                        button -> this.close()
                )
                .dimensions(
                        this.width / 2 - 75,
                        this.height / 2 + 50,
                        150,
                        20
                )
                .build();

        this.addDrawableChild(cancelButton);
    }

    private void onSendButtonPressed() {
        String text = textField.getText().trim();

        if (text.isEmpty()) {
            LOGGER.warn("Попытка отправить пустое сообщение");
            return;
        }

        try {
            MessageProto.Message protoMessage = MessageProto.Message.newBuilder()
                    .setText(text)
                    .build();

            byte[] messageBytes = protoMessage.toByteArray();

            LOGGER.info("Отправка сообщения на сервер: {}", text);

            ClientPlayNetworking.send(new NetworkHandler.MessagePayload(messageBytes));

            this.close();

        } catch (Exception e) {
            LOGGER.error("Ошибка при отправке сообщения", e);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                this.height / 2 - 40,
                0xFFFFFF
        );

        textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        // Enter для отправки
        if (keyCode == 257) {
            onSendButtonPressed();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (textField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        textField.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}