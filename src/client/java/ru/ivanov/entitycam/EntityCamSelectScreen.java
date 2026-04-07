package ru.ivanov.entitycam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class EntityCamSelectScreen extends Screen {
    private static final int SEARCH_RADIUS_BLOCKS = 64;
    private static final int ITEM_HEIGHT = 22;

    private TextFieldWidget filter;
    private EntityList list;
    private int lastEntityCount;

    public EntityCamSelectScreen() {
        super(Text.literal("EntityCam"));
    }

    @Override
    protected void init() {
        int top = 20;

        filter = new TextFieldWidget(textRenderer, 10, top, width - 20, 20, Text.literal("Filter"));
        filter.setChangedListener(ignored -> refresh());
        addDrawableChild(filter);

        int listTop = top + 24 + 28;
        int listHeight = height - listTop - 40;

        list = new EntityList(client, width - 20, listHeight, listTop, ITEM_HEIGHT);
        list.setX(10);
        addDrawableChild(list);

        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), b -> refresh())
            .dimensions(10, height - 30, 90, 20)
            .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back to you"), b -> {
            if (client != null && client.player != null) client.setCameraEntity(client.player);
            close();
        }).dimensions(110, height - 30, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
            .dimensions(width - 100, height - 30, 90, 20)
            .build());

        refresh();
    }

    private void refresh() {
        if (client == null || client.player == null || client.world == null) return;

        String q = filter.getText() == null ? "" : filter.getText().trim().toLowerCase(Locale.ROOT);

        Box box = client.player.getBoundingBox().expand(SEARCH_RADIUS_BLOCKS);
        List<Entity> entities = client.world.getOtherEntities(client.player, box, e -> e != null && e.isAlive());
        entities.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(client.player)));

        List<Entity> filtered = new ArrayList<>(entities.size());
        for (Entity e : entities) {
            if (entityMatchesFilter(e, q)) filtered.add(e);
        }

        lastEntityCount = filtered.size();
        list.setEntities(filtered);
    }

    private static boolean entityMatchesFilter(Entity e, String q) {
        if (q.isEmpty()) return true;

        String name = e.getName().getString().toLowerCase(Locale.ROOT);
        if (name.contains(q)) return true;

        String typeStr = e.getType().toString().toLowerCase(Locale.ROOT);
        if (typeStr.contains(q)) return true;

        Identifier id = Registries.ENTITY_TYPE.getId(e.getType());
        if (id != null) {
            String idFull = id.toString().toLowerCase(Locale.ROOT);
            String path = id.getPath().toLowerCase(Locale.ROOT);
            if (idFull.contains(q) || path.contains(q)) return true;
        }

        String transKey = e.getType().getTranslationKey().toLowerCase(Locale.ROOT);
        return transKey.contains(q);
    }

    @Override
    public void render(DrawContext context
