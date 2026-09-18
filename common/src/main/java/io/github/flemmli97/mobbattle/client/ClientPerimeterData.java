package io.github.flemmli97.mobbattle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.mobbattle.common.registry.MobBattleItems;
import io.github.flemmli97.mobbattle.common.utils.PerimeterData;
import io.github.flemmli97.mobbattle.common.utils.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class ClientPerimeterData {

    private static PerimeterData.Perimeter perimeter;

    public static PerimeterData.Perimeter getPerimeter() {
        return perimeter;
    }

    public static void setPerimeter(PerimeterData.Perimeter perimeter) {
        ClientPerimeterData.perimeter = perimeter;
    }

    public static void render(Player player, PoseStack stack, SubmitNodeCollector collector, LevelRenderState levelRenderState) {
        if (perimeter == null)
            return;
        if (player.getMainHandItem().is(MobBattleItems.PERIMETER_TOOL.get()) || player.getOffhandItem().is(MobBattleItems.PERIMETER_TOOL.get())) {
            AABB aabb = Utils.getBoundingBoxPositions(perimeter.center(), null);
            ClientHandler.renderShapeOutline(stack, collector, Shapes.create(aabb), ARGB.colorFromFloat(0.5F, 0.5F, 1, 1));
            Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
            Vec3 vec = camera.position();
            stack.pushPose();
            stack.translate(-vec.x, -vec.y, -vec.z);
            DrawableGizmoPrimitives gizmos = new DrawableGizmoPrimitives();
            switch (perimeter.shape()) {
                case CIRCLE -> {
                    double pY = Mth.lerp(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false),
                            Minecraft.getInstance().player.yOld, Minecraft.getInstance().player.getY());
                    float minY = Minecraft.getInstance().level.getMinY();
                    Vec3 center = Vec3.atCenterOf(perimeter.center());
                    if (perimeter.inner() != perimeter.outer()) {
                        renderCylinder(gizmos, center, perimeter.inner(), minY, (float) pY, 1, 1, 0, 0.3F);
                    }
                    renderCylinder(gizmos, center, perimeter.outer(), minY, (float) pY, 0.5F, 0, 0, 0.3F);
                }
                case SQUARE -> {
                    double pY = Mth.lerp(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false),
                            Minecraft.getInstance().player.yOld, Minecraft.getInstance().player.getY());
                    AABB perimeterAABB = new AABB(perimeter.center().getX(), Minecraft.getInstance().level.getMinY(), perimeter.center().getZ(),
                            perimeter.center().getX() + 1, pY + 1, perimeter.center().getZ() + 1);
                    if (perimeter.inner() != perimeter.outer()) {
                        renderWall(gizmos, perimeterAABB.inflate(perimeter.inner(), 0, perimeter.inner()), 1, 1, 0, 0.3F);
                    }
                    renderWall(gizmos, perimeterAABB.inflate(perimeter.outer(), 0, perimeter.outer()), 0.5F, 0, 0, 0.3F);
                }
            }
            gizmos.submit(collector, levelRenderState.cameraRenderState, false);
            stack.popPose();
        }
    }

    private static void renderWall(DrawableGizmoPrimitives gizmos, AABB aabb, float red, float green, float blue, float alpha) {
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;
        // West side
        int color = ARGB.colorFromFloat(alpha, red, green, blue);
        gizmos.addQuad(new Vec3(minX, minY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(minX, maxY, minZ), color);
        gizmos.addQuad(new Vec3(minX, maxY, minZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, minY, minZ), color);

        // East side
        gizmos.addQuad(new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, maxY, maxZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(maxX, minY, minZ), color);
        gizmos.addQuad(new Vec3(maxX, minY, minZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(maxX, maxY, maxZ),
                new Vec3(maxX, maxY, minZ), color);

        // North side
        gizmos.addQuad(new Vec3(minX, maxY, minZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(minX, minY, minZ), color);
        gizmos.addQuad(new Vec3(minX, minY, minZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(minX, maxY, minZ), color);

        // South side
        gizmos.addQuad(new Vec3(maxX, maxY, maxZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(maxX, minY, maxZ), color);
        gizmos.addQuad(new Vec3(maxX, minY, maxZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, maxY, maxZ), color);
    }


    private static void renderCylinder(DrawableGizmoPrimitives gizmos, Vec3 pos, double radius,
                                       float minY, float maxY,
                                       float red, float green, float blue, float alpha) {
        int color = ARGB.colorFromFloat(alpha, red, green, blue);
        int precision = 24;
        float step = Mth.PI / precision;
        for (int i = 0; i < precision * 2; ++i) {
            float theta = i * step;
            float thetaNext = theta + step;
            float x = (float) (pos.x() + radius * Math.cos(theta));
            float z = (float) (pos.z() + radius * Math.sin(theta));
            float xN = (float) (pos.x() + radius * Math.cos(thetaNext));
            float zN = (float) (pos.z() + radius * Math.sin(thetaNext));
            gizmos.addQuad(new Vec3(x, minY, z),
                    new Vec3(x, maxY, z),
                    new Vec3(xN, maxY, zN),
                    new Vec3(xN, minY, zN), color);

            gizmos.addQuad(new Vec3(xN, minY, zN),
                    new Vec3(xN, maxY, zN),
                    new Vec3(x, maxY, z),
                    new Vec3(x, minY, z), color);
        }
    }
}
