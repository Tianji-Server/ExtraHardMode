package com.extrahardmode.mocks;


import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Minimal {@link ServerBuildInfo} for the unit tests.
 * <p/>
 * paper-api looks its implementation up via the ServiceLoader ({@code ServerBuildInfo#buildInfo()}), which only
 * exists inside a real server. Outside of one the lookup throws, so the tests register this stub through
 * {@code META-INF/services/io.papermc.paper.ServerBuildInfo}.
 */
public final class TestServerBuildInfo implements ServerBuildInfo
{
    @Override
    public Key brandId()
    {
        return BRAND_PAPER_ID;
    }


    @Override
    public boolean isBrandCompatible(Key key)
    {
        return BRAND_PAPER_ID.equals(key);
    }


    @Override
    public String brandName()
    {
        return "TestPaper";
    }


    @Override
    public String minecraftVersionId()
    {
        return "test";
    }


    @Override
    public String minecraftVersionName()
    {
        return "test";
    }


    @Override
    public OptionalInt buildNumber()
    {
        return OptionalInt.of(1);
    }


    @Override
    public Instant buildTime()
    {
        return Instant.EPOCH;
    }


    @Override
    public Optional<String> gitBranch()
    {
        return Optional.empty();
    }


    @Override
    public Optional<String> gitCommit()
    {
        return Optional.empty();
    }


    @Override
    public String asString(StringRepresentation representation)
    {
        return brandName() + " " + minecraftVersionId();
    }
}
