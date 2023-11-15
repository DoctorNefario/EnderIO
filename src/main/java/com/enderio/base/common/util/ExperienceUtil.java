package com.enderio.base.common.util;

import com.enderio.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import net.minecraft.world.entity.player.Player;

public class ExperienceUtil {

    // 1 exp = 20 mb
    public static final int EXP_TO_FLUID = 20;

    /**
     * What the maximum level the player should receive is.
     * <p>
     * The survival limit is 238,609,312
     */
    public static final int MAX_PLAYER_LEVEL_GIVE = 238_609_312;

    /**
     * What the maximum level this handles should be.
     * <p>
     * Values higher than 320,127,978 may overflow.
     */
    public static final int MAX_TANK_LEVEL = 320_127_978;
    public static final long MAX_TANK_MB = getTotalPointsFromLevel(MAX_TANK_LEVEL) * EXP_TO_FLUID;
    public static final int MAX_TANK_MB_INT = (int) Math.min(MAX_TANK_MB, Integer.MAX_VALUE);

    /**
     * Vanilla way of calculating experience points required for level up.
     * @param currentLevel - the current level of player. The level up cost depends on currentLevel and not on the level you are trying to reach
     * @return experience - experience cost to level up
     */
    public static long getXpNeededForNextLevel(long currentLevel) {
        if (currentLevel >= 30) {
            return 112 + (currentLevel - 30) * 9;
        } else {
            return currentLevel >= 15 ? 37 + (currentLevel - 15) * 5 : 7 + currentLevel * 2;
        }
    }

    public static long getMbNeededForNextLevel(long currentLevel) {
        return getXpNeededForNextLevel(currentLevel) * EXP_TO_FLUID;
    }

    /**
     * Returns the total xp required to reach the target level ( from 0 )
     * @param level - level to reach
     * @return experience - total xp cost
     */
    public static long getTotalPointsFromLevel(long level) {
        if (level >= 32) {
            return (9 * level * level - 325 * level) / 2 + 2220;
        } else if (level >= 17) {
            return (5 * level * level - 81 * level) / 2 + 360;
        } else {
            return level * level + 6 * level;
        }
    }

    /**
     * Returns the max level attainable using all the experience
     * @param experience - total experience
     * @return level - max level
     */
    public static int getTotalLevelFromPoints(long experience) {
        int estimatedLevel = estimateTotalLevelFromPoints(experience);

        long estimatedXp = getTotalPointsFromLevel(estimatedLevel);

        // estimation should always be within a few levels of the actual level
        if (experience - estimatedXp < 0) {
            estimatedLevel += 1;
        } else if (experience - estimatedXp > getXpNeededForNextLevel(estimatedLevel)) {
            estimatedLevel -= 1;
        }

        return estimatedLevel;
    }

    /**
     * Estimates the max level attainable using all the experience
     *
     * @param experience Total experience
     * @return The max level, within margin of error
     */
    public static int estimateTotalLevelFromPoints(long experience) {
        if (experience >= 1508) {
            return (int) ((325d / 18d) + (Math.sqrt((2d / 9d) * (experience - (54215d / 72d)))));
        } else if (experience >= 353) {
            return (int) ((81d / 10d) + (Math.sqrt((2d / 5d) * (experience - (7839d / 40d)))));
        } else {
            return (int) (Math.sqrt(experience + 9d) - 3d);
        }
    }

    public static int lvlToMb(int level) {
        return SimpleXpFluid.fromLevel(level).mbInt();
    }

    public static int mbToLvl(int mb) {
        return new SimpleXpFluid(mb).mbInt();
    }

    /**
     * Wrapper for long to simplify XP fluid handling
     * @param millibuckets Always between 0 and {@link ExperienceUtil#MAX_TANK_MB MAX_TANK_MB}
     */
    public record SimpleXpFluid(long millibuckets) {
        public SimpleXpFluid(long millibuckets) {
            // should always be within these values
            this.millibuckets = Math.min(Math.max(millibuckets, 0), MAX_TANK_MB);
        }

        /**
         * @param xp The experience to convert to fluid
         * @return The total millibuckets to reach the given XP
         */
        public static SimpleXpFluid fromXp(long xp) {
            return new SimpleXpFluid(xp * ExperienceUtil.EXP_TO_FLUID);
        }

        /**
         * @param level The level to convert to fluid
         * @return The total millibuckets it takes to reach the given level
         */
        public static SimpleXpFluid fromLevel(long level) {
            return fromXp(ExperienceUtil.getTotalPointsFromLevel(level));
        }

        /**
         * @param player The player to inspect
         * @return The player's XP represented as a fluid
         */
        public static SimpleXpFluid fromPlayer(Player player) {
            int level = Math.min(player.experienceLevel, MAX_TANK_LEVEL);
            float progress = player.experienceProgress;

            long xp = ExperienceUtil.getTotalPointsFromLevel(level) + (long) Math.floor(ExperienceUtil.getXpNeededForNextLevel(level) * progress);

            return fromXp(xp);
        }

        /**
         * @return The amount of experience points this contains
         */
        public long points() {
            return millibuckets() / ExperienceUtil.EXP_TO_FLUID;
        }

        /***
         * @return The experience points that aren't included in levels
         */
        public long leftoverPoints() {
            return points() - ExperienceUtil.getTotalPointsFromLevel(level());
        }

        /**
         * @return The millibuckets less than a single experience point
         */
        public int leftoverMb() {
            return (int) (millibuckets() % ExperienceUtil.EXP_TO_FLUID);
        }

        /**
         * @return The equivalent of {@link Player#experienceLevel}
         */
        public int level() {
            return ExperienceUtil.getTotalLevelFromPoints(points());
        }

        /**
         * @return The equivalent of {@link Player#experienceProgress}
         */
        public double levelProgress() {
            return (leftoverPoints() * EXP_TO_FLUID + leftoverMb()) / (double) getMbNeededForNextLevel(level());
        }

        /**
         * @return The millibuckets as an int, saturates at max value
         */
        public int mbInt() {
            return (int) Math.min(millibuckets(), (Integer.MAX_VALUE / 20) * 20);
        }

        public SimpleXpFluid saturatingAdd(long val) {
            if (val < 0) {
                return saturatingSub(-val);
            }

            // overflow resistant comparison
            if (MAX_TANK_MB - millibuckets() < val) {
                return new SimpleXpFluid(MAX_TANK_MB);
            }

            return new SimpleXpFluid(millibuckets() + val);
        }

        public SimpleXpFluid saturatingSub(long val) {
            if (val < 0) {
                return saturatingAdd(-val);
            }

            return new SimpleXpFluid(Math.max(millibuckets() - val, 0));
        }

        /**
         * @param player     The player to give/take levels to/from
         * @param levelCount The amount of levels you want to add/remove
         * @return The new {@link SimpleXpFluid} with fluid taken or removed. Value MUST be used to avoid duplicating XP.
         */
        public SimpleXpFluid useToAdjustPlayerLevel(Player player, int levelCount) {
            if (player.experienceLevel > MAX_TANK_LEVEL) {
                // just void the excess, that's way too much XP.
                player.experienceLevel = MAX_TANK_LEVEL;
            }

            if (levelCount > 0) {
                levelCount = (int) Math.min((long) levelCount + player.experienceLevel, MAX_PLAYER_LEVEL_GIVE) - player.experienceLevel;
                 if (levelCount == 0) {
                     player.displayClientMessage(EIOLang.TOO_MANY_LEVELS, true);
                     return this;
                 }
            }

            SimpleXpFluid curPlayerXp = fromPlayer(player);

            int newLevel = (int) Math.min(Math.max((long) player.experienceLevel + levelCount, 0), MAX_TANK_LEVEL);

            long newPlayerMb = fromLevel(newLevel).millibuckets();
            long mbDifference = curPlayerXp.millibuckets() - newPlayerMb;
            long newTankMb;
            if (MAX_TANK_MB - millibuckets() < mbDifference) {
                // this will also void excess experience
                newTankMb = MAX_TANK_MB;
            } else {
                newTankMb = millibuckets() + mbDifference;
            }


            if (newTankMb < 0) {
                newPlayerMb += newTankMb;
                newTankMb = 0;
            }

            SimpleXpFluid newPlayerXp = new SimpleXpFluid(newPlayerMb);
            setPlayerExperience(player, newPlayerXp);

            return new SimpleXpFluid(newTankMb);
        }

        public void addToPlayer(Player player) {
            SimpleXpFluid currentXp = fromPlayer(player);
            long newPlayerMb = currentXp.millibuckets() + millibuckets();

            SimpleXpFluid newPlayerXp = new SimpleXpFluid(newPlayerMb);
            setPlayerExperience(player, newPlayerXp);
        }

        public void subtractFromPlayer(Player player) {
            SimpleXpFluid currentXp = fromPlayer(player);
            long newPlayerMb = currentXp.millibuckets() - millibuckets();
            if (newPlayerMb < 0) {
                EnderIO.LOGGER.warn("Player XP too low, this is a bug.");
                newPlayerMb = 0;
            }

            SimpleXpFluid newPlayerXp = new SimpleXpFluid(newPlayerMb);
            setPlayerExperience(player, newPlayerXp);
        }
    }

    private static void setPlayerExperience(Player player, SimpleXpFluid targetXp) {
        // If there are any XP mod compatibility issues this might be the cause
        player.experienceLevel = targetXp.level();
        player.experienceProgress = (float) targetXp.levelProgress();

        // Required to update experience values
        player.giveExperiencePoints(0);
    }

}
