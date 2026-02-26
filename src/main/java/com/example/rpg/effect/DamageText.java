package com.example.rpg.effect;

public class DamageText {
    public double x, y, z;
    public float amount;

    private int ticksAlive;
    private final int maxTicks;

    public DamageText(double x, double y, double z, float amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = amount;

        this.ticksAlive = 0;
        this.maxTicks = (amount > 15) ? 40 : 25; // Сильные удары живут дольше
    }

    public void tick() {
        this.ticksAlive++;
        // Медленно поднимается вверх
        this.y += 0.03;
    }

    public boolean isDead() {
        return ticksAlive >= maxTicks;
    }

    public float getAlpha() {
        if (ticksAlive < maxTicks / 2)
            return 1.0f;
        float remaining = maxTicks - ticksAlive;
        return remaining / (maxTicks / 2.0f);
    }

    public float getScale() {
        // Удары сильнее - цифры больше.
        float baseScale = (amount > 15) ? 1.5f : (amount > 5 ? 1.0f : 0.8f);

        // Пружинящая анимация при появлении
        if (ticksAlive < 5) {
            return baseScale * (0.5f + (ticksAlive / 5.0f) * 0.7f);
        } else if (ticksAlive < 8) {
            return baseScale * (1.2f - ((ticksAlive - 5) / 3.0f) * 0.2f);
        }
        return baseScale;
    }
}
