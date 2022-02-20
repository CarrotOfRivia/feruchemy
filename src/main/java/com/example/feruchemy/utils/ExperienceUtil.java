package com.example.feruchemy.utils;
/*
 * MIT License
 *
 * Copyright (c) 2019 EideeHi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import java.util.stream.IntStream;

import net.minecraft.world.entity.player.Player;

public class ExperienceUtil
{
    private ExperienceUtil()
    {
    }

    public static int expBarCap( int level )
    {
        if ( level >= 30 )
        {
            return 112 + ( level - 30 ) * 9;
        }
        else
        {
            return level >= 15 ? 37 + ( level - 15 ) * 5 : 7 + level * 2;
        }
    }

    public static int expToLevel( int exp )
    {
        int level = 0;
        int _exp = exp;
        int cap = expBarCap( level );
        while ( _exp >= cap )
        {
            _exp -= cap;
            cap = expBarCap( ++level );
        }
        return level;
    }

    public static int levelToExp( int level, float expBar )
    {
        int sum = IntStream.range( 0, level ).map( ExperienceUtil::expBarCap ).sum();
        return sum + Math.round( expBarCap( level ) * expBar );
    }

    public static int getPlayerExp( Player player )
    {
        return levelToExp( player.experienceLevel, player.experienceProgress );
    }

    public static void addExpToPlayer( Player player, int value )
    {
        player.increaseScore( value );

        int playerExp = getPlayerExp( player );
        int limit = Integer.MAX_VALUE - playerExp;
        int _value = Math.min( value, limit );
        int exp = playerExp + _value;
        int level = expToLevel( exp );
        float rest = exp - levelToExp( level, 0.0F );

        player.totalExperience += _value;
        player.experienceLevel = level;
        player.experienceProgress = rest / expBarCap( level );
    }


    public static boolean removeExpFromPlayer( Player player, int value )
    {
        int exp = getPlayerExp( player );

        player.increaseScore( -exp );
        player.totalExperience -= exp;
        player.experienceLevel = 0;
        player.experienceProgress = 0;

        if ( exp > value )
        {
            addExpToPlayer( player, exp - value );
            return true;
        }
        else {
            return false;
        }
    }
}