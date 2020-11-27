package com.github.alex1304.jdash.util;


import com.github.alex1304.jdash.entity.GDSong;
import com.github.alex1304.jdash.entity.Length;

import java.util.Objects;
import java.util.Optional;

/**
 * Allows to define upload options for levels.
 */
public final class LevelUploadOptions {

    private String levelDesc;
    private Optional<Length> length;
    private Optional<GDSong> song;
    private Optional<Boolean> auto;
    private boolean copyable;
    private int password;
    private Optional<Boolean> twoPlayer;
    private long original;
    private Optional<Integer> objects;
    private Optional<Integer> coins;
    private int requestStars;
    private boolean unlisted;

    private LevelUploadOptions(String levelDesc, Optional<Length> length, Optional<GDSong> song, Optional<Boolean> auto,
                               boolean copyable, int password, Optional<Boolean> twoPlayer, long original, Optional<Integer> objects,
                               Optional<Integer> coins, int requestStars, boolean unlisted) {
        this.levelDesc = levelDesc;
        this.length = length;
        this.song = song;
        this.auto = auto;
        this.copyable = copyable;
        this.password = password;
        this.twoPlayer = twoPlayer;
        this.original = original;
        this.objects = objects;
        this.coins = coins;
        this.requestStars = requestStars;
        this.unlisted = unlisted;
    }

    /**
     * Creates a new instance of upload options.
     *
     * @return a new instance of {@link LevelUploadOptions}
     */
    public static LevelUploadOptions create() {
        return new LevelUploadOptions("", Optional.empty(), Optional.empty(), Optional.empty(),
                false, 0, Optional.empty(), 0, Optional.empty(),
                Optional.empty(), 0, false);
    }


    public LevelUploadOptions setDescription(String desc) {
        this.levelDesc = Objects.requireNonNull(desc);
        return this;
    }

    public LevelUploadOptions defineLength(Length len) {
        this.length = Optional.of(Objects.requireNonNull(len));
        return this;
    }

    public LevelUploadOptions defineSong(GDSong song) {
        this.song = Optional.of(Objects.requireNonNull(song));
        return this;
    }

    public LevelUploadOptions defineAuto(boolean auto) {
        this.auto = Optional.of(auto);
        return this;
    }

    public LevelUploadOptions toggleCopy(boolean copyable) {
        this.copyable = copyable;
        return this;
    }

    public LevelUploadOptions setPassword(int password) {
        if(password < 1_000 || password > 1_000_000) throw new IllegalArgumentException("Password must be 4-6 digits.");
        this.password = password;
        return this;
    }

    public LevelUploadOptions defineTwoPlayer(boolean twoPlayer) {
        this.twoPlayer = Optional.of(twoPlayer);
        return this;
    }

    public LevelUploadOptions setOriginalLevel(long levelId) {
        this.original = levelId;
        return this;
    }

    public LevelUploadOptions defineObjectCount(int count) {
        this.objects = Optional.of(count);
        return this;
    }

    public LevelUploadOptions defineCoinCount(int count) {
        this.coins = Optional.of(count);
        return this;
    }

    public LevelUploadOptions setRequestStars(int stars) {
        if(stars < 0 || stars > 10) throw new IllegalArgumentException("Stars must be between 0 and 10.");
        this.requestStars = stars;
        return this;
    }

    public LevelUploadOptions toggleUnlisted(boolean unlisted) {
        this.unlisted = unlisted;
        return this;
    }

    public String getLevelDesc() {
        return levelDesc;
    }

    public Optional<Length> getLength() {
        return length;
    }

    public Optional<GDSong> getSong() {
        return song;
    }

    public Optional<Boolean> getAuto() {
        return auto;
    }

    public boolean isCopyable() {
        return copyable;
    }

    public int getPassword() {
        return password;
    }

    public Optional<Boolean> getTwoPlayer() {
        return twoPlayer;
    }

    public long getOriginal() {
        return original;
    }

    public Optional<Integer> getObjects() {
        return objects;
    }

    public Optional<Integer> getCoins() {
        return coins;
    }

    public int getRequestStars() {
        return requestStars;
    }

    public boolean isUnlisted() {
        return unlisted;
    }

    public boolean isQualified() {
        return length.isPresent() && song.isPresent() && auto.isPresent() && twoPlayer.isPresent()
                && objects.isPresent() && coins.isPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, song, auto, copyable, password, twoPlayer, original, objects, coins, requestStars, unlisted);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LevelUploadOptions)) return false;
        LevelUploadOptions o = (LevelUploadOptions) obj;
        return copyable == o.copyable &&
                password == o.password &&
                original == o.original &&
                requestStars == o.requestStars &&
                unlisted == o.unlisted &&
                Objects.equals(levelDesc, o.levelDesc) &&
                Objects.equals(length, o.length) &&
                Objects.equals(song, o.song) &&
                Objects.equals(auto, o.auto) &&
                Objects.equals(twoPlayer, o.twoPlayer) &&
                Objects.equals(objects, o.objects) &&
                Objects.equals(coins, o.coins);
    }
}
