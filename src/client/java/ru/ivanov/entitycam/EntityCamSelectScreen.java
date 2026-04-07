private final class EntityEntry extends ElementListWidget.Entry<EntityEntry> {
		private final Entity entity;

		private EntityEntry(Entity entity) {
			this.entity = entity;
		}

		@Override
		public List<? extends net.minecraft.client.gui.Element> children() {
			return List.of();
		}

		@Override
		public List<net.minecraft.client.gui.Selectable> selectableChildren() {
			return List.of();
		}

		// В 1.21.1 у метода render другие параметры (добавлены index, y, x и размеры)
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (client == null || client.player == null) return;

			double d = Math.sqrt(entity.squaredDistanceTo(client.player));
			String label = entity.getName().getString() + "  (" + String.format(Locale.ROOT, "%.1f", d) + "m)";

			// Используем координаты x и y, которые передает сам список для этой строки
			int textX = x + 6;
			int textY = y + (entryHeight - textRenderer.fontHeight) / 2;

			context.drawTextWithShadow(textRenderer, Text.literal(label), textX, textY, hovered ? 0xFFFFAA : 0xFFFFFF);
		}

		// В 1.21.1 mouseClicked принимает координаты double и код кнопки int
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (client == null) return false;
			
			// 0 — это левая кнопка мыши
			if (button != 0) return false;

			if (entity.isAlive()) {
				client.setCameraEntity(entity);
				close();
				return true;
			}
			return false;
		}
	}
