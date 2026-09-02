package com.conquest.hud.client.render.nanovg;

import net.minecraft.client.MinecraftClient;

public class NanoVGManager {
    private static long vg;

    public static void init() {
        vg = org.lwjgl.nanovg.NanoVGGL3.nvgCreate(org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS | org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES);
        if (vg == 0) {
            System.err.println("Could not init NanoVG");
        }
    }

    public static void setupAndDraw(Runnable drawCode) {
        if (vg == 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int winWidth = client.getWindow().getScaledWidth();
        int winHeight = client.getWindow().getScaledHeight();
        float pixelRatio = (float) client.getWindow().getFramebufferWidth() / client.getWindow().getWidth();

        com.mojang.blaze3d.systems.RenderSystem.assertOnRenderThread();

        int lastProgram = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM);
        int lastVao = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL30.GL_VERTEX_ARRAY_BINDING);
        int lastArrayBuffer = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER_BINDING);
        int lastActiveTexture = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_ACTIVE_TEXTURE);
        int lastTexture = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);

        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);

        org.lwjgl.nanovg.NanoVG.nvgBeginFrame(vg, winWidth, winHeight, pixelRatio);
        drawCode.run();
        org.lwjgl.nanovg.NanoVG.nvgEndFrame(vg);

        org.lwjgl.opengl.GL20.glUseProgram(lastProgram);
        org.lwjgl.opengl.GL30.glBindVertexArray(lastVao);
        org.lwjgl.opengl.GL15.glBindBuffer(org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER, lastArrayBuffer);
        org.lwjgl.opengl.GL20.glActiveTexture(lastActiveTexture);
        org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, lastTexture);

        // --- ЖЕСТКИЙ СБРОС OPENGL ДЛЯ ФИКСА ДВОЙНЫХ ТЕКСТУР ---
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);

        // Восстанавливаем ориентацию полигонов и отбраковку (Culling), которую ломает NanoVG
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        org.lwjgl.opengl.GL11.glFrontFace(org.lwjgl.opengl.GL11.GL_CCW);
        org.lwjgl.opengl.GL11.glCullFace(org.lwjgl.opengl.GL11.GL_BACK);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        org.lwjgl.opengl.GL11.glDepthMask(true);

        com.mojang.blaze3d.systems.RenderSystem.enableCull();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    }

    public static void drawRoundedRect(int x, int y, int w, int h, float r, int color) {
        org.lwjgl.nanovg.NanoVG.nvgBeginPath(vg);
        org.lwjgl.nanovg.NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
        org.lwjgl.nanovg.NanoVG.nvgFillColor(vg, rgba(color));
        org.lwjgl.nanovg.NanoVG.nvgFill(vg);
    }

    public static void drawRoundedRectStroke(int x, int y, int w, int h, float r, float strokeWidth, int color) {
        org.lwjgl.nanovg.NanoVG.nvgBeginPath(vg);
        org.lwjgl.nanovg.NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
        org.lwjgl.nanovg.NanoVG.nvgStrokeWidth(vg, strokeWidth);
        org.lwjgl.nanovg.NanoVG.nvgStrokeColor(vg, rgba(color));
        org.lwjgl.nanovg.NanoVG.nvgStroke(vg);
    }

    private static org.lwjgl.nanovg.NVGColor rgba(int color) {
        org.lwjgl.nanovg.NVGColor nvgColor = org.lwjgl.nanovg.NVGColor.create();
        org.lwjgl.nanovg.NanoVG.nvgRGBA(
                (byte) ((color >> 16) & 0xFF),
                (byte) ((color >> 8) & 0xFF),
                (byte) (color & 0xFF),
                (byte) ((color >> 24) & 0xFF),
                nvgColor
        );
        return nvgColor;
    }
}