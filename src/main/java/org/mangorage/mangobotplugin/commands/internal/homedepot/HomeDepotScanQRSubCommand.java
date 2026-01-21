package org.mangorage.mangobotplugin.commands.internal.homedepot;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import net.dv8tion.jda.api.entities.Message;
import org.mangorage.mangobotcore.api.command.v1.CommandContext;
import org.mangorage.mangobotcore.api.command.v1.CommandParseResult;
import org.mangorage.mangobotcore.api.jda.command.v2.AbstractJDACommand;
import org.mangorage.mangobotcore.api.jda.command.v2.JDACommandResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public final class HomeDepotScanQRSubCommand extends AbstractJDACommand {
    public HomeDepotScanQRSubCommand() {
        super("scanqr");
    }

    @Override
    public JDACommandResult run(Message message, CommandContext commandContext, CommandParseResult commandParseResult) throws Throwable {
        final var attachment = message.getAttachments().getFirst();
        if (attachment != null) {
            message.reply(
                    readQrFromUrl(attachment.getUrl())
            ).queue();
        }
        return JDACommandResult.PASS;
    }

    public static String readQrFromUrl(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);

        try (InputStream is = url.openStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IllegalArgumentException("Not an image. Try again.");
            }

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        }
    }
}
