package com.conquest.hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class NanoVGHelper {
    public static final NanoVGHelper INSTANCE = new NanoVGHelper();
    private long vg;
    private NVGColor color;
    private boolean initialized = false;

    public void init() {
        if (initialized) return;
        RenderSystem.assertOnRenderThread();
        this.vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
        this.color = NVGColor.create();
        this.initialized = true;
    }

    public void beginFrame(int width, int height) {
        if (!initialized) init();
        // Временно отключаем ванильный шейдерный контекст для чистой отрисовки NanoVG
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        NanoVG.nvgBeginFrame(vg, width, height, 1.0f);
    }

    public void endFrame() {
        NanoVG.nvgEndFrame(vg);

        // Принудительный сброс состояния OpenGL для корректной работы ванильного рендера
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public void drawRoundedRect(float x, float y, float w, float h, float radius, int hexColor, float alpha) {
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, w, h, radius);

        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;

        color.r(r).g(g).b(b).a(alpha);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }
    public void cleanup() {
        if (this.vg != 0) {
            org.lwjgl.nanovg.NanoVGGL3.nvgDelete(this.vg);
            this.vg = 0;
        }
    }
}