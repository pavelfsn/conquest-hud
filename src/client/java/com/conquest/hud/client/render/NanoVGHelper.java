package com.conquest.hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL30.*;

public class NanoVGHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("NanoVGHelper");
    public static final NanoVGHelper INSTANCE = new NanoVGHelper();
    private long vg;
    private NVGColor color;
    private boolean initialized = false;

    public void init() {
        if (initialized) return;
        try {
            RenderSystem.assertOnRenderThread();
            this.vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
            if (this.vg == 0L) {
                LOGGER.error("Failed to create NanoVG context");
                return;
            }
            this.color = NVGColor.create();
            this.initialized = true;
            LOGGER.info("NanoVG context created");
        } catch (Exception e) {
            LOGGER.error("Exception during NanoVG initialization", e);
        }
    }

    public void beginFrame(int width, int height) {
        if (!initialized) {
            init();
            if (!initialized) return;
        }
        // NanoVG сам управляет состоянием, не нужно сохранять
        NanoVG.nvgBeginFrame(vg, width, height, 1.0f);
    }

    public void endFrame() {
        if (!initialized) return;
        NanoVG.nvgEndFrame(vg);
        // Сбрасываем VAO, чтобы ванильный рендерер работал корректно
        glBindVertexArray(0);
    }

    public void drawRoundedRect(float x, float y, float w, float h, float radius, int hexColor, float alpha) {
        if (!initialized) return;
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
        if (this.vg != 0L) {
            NanoVGGL3.nvgDelete(this.vg);
            this.vg = 0L;
            this.initialized = false;
            LOGGER.info("NanoVG context cleaned up");
        }
    }

    public boolean isInitialized() {
        return initialized && vg != 0L;
    }
}