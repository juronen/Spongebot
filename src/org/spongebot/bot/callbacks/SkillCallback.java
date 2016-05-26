package org.spongebot.bot.callbacks;

public class SkillCallback {

    public static void updateExperience(int skillID, int amount) {
        System.out.println("[SkillCallback] Gained " + amount + " experience in skill #" + skillID);
    }

    public static void updateCurrentLevel(int skillID, int amount) {
        System.out.println("[SkillCallback] Restored " + amount + " levels in skill #" + skillID);
    }

    public static void updateMaxLevel(int skillID, int amount) {
        System.out.println("[SkillCallback] Gained " + amount + " levels in skill #" + skillID);

    }
}
