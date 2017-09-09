package com.android.lewis.longestword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;

/*
    A Java Enumeration implementation to return combinations of characters in any string (at least one-character long), without any duplication caused by repeated characters.
    Copyright (C) 2013 Lewis Tat Fong Choo Man

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see http://www.gnu.org/licenses/gpl.html.
 */

public class Combinations implements Enumeration<String> {

    private List<CombinationCharDomain> combinationCharDomains = new ArrayList<CombinationCharDomain>();
    private int combinationCharCount;
    private int startChar = 0;
    private StringBuilder currentCombination;

    public Combinations (String s, int combinationCharCount)
    {
        if (s == null)
        {
            throw new NullPointerException();
        }
        int sLen = s.length();

        if (sLen <= 0)
        {
            throw new IllegalArgumentException("Length of domain string cannot be zero");
        }
		else if (combinationCharCount <= 0)
		{
            throw new IllegalArgumentException("Combination character count cannot be zero or less");
		}
		else if (combinationCharCount >= sLen)
		{
            throw new IllegalArgumentException("Combination character count cannot be higher or equal to the length of domain string");
        }

        this.combinationCharDomains.add(new CombinationCharDomain(s, 0));
        for (int i = 1; i < combinationCharCount; i++)
        {
            this.combinationCharDomains.add(new CombinationCharDomain(i));
        }
        this.combinationCharCount = combinationCharCount;
        this.currentCombination = new StringBuilder(s.substring(0, combinationCharCount));
    }

    private class MapEntry<K, V> implements Map.Entry<K, V>
    {
    	K key;
    	V value;
    	
    	public MapEntry(K key, V value)
    	{
            this.key = key;
            this.value = value;
    	}

		@Override
		public K getKey() {
			// TODO Auto-generated method stub
			return this.key;
		}

		@Override
		public V getValue() {
			// TODO Auto-generated method stub
			return this.value;
		}

		@Override
		public V setValue(V value) {
			// TODO Auto-generated method stub
			return (this.value = value);
		}
    }
    
    private class CombinationCharDomain implements Enumeration<Character> {
        private int currentIndex = -1, charPos;
        private List<Map.Entry<Character, Integer>> charMapList = new ArrayList<Map.Entry<Character, Integer>>();

        public CombinationCharDomain(int charPos)
        {
		    this.charPos = charPos;
        }
        
        public CombinationCharDomain(String s, int charPos)
        {
		    this(charPos);
            char[] chars = s.toCharArray();
            char prevChar;
            int countConsecutive = 1;

            Arrays.sort(chars);
            prevChar = chars[0];
            for (int i = 1; i < chars.length; i++)
            {
                if (chars[i] != prevChar)
                {
                    this.charMapList.add(new MapEntry<Character, Integer>(prevChar, countConsecutive));
                    countConsecutive = 1;
                    prevChar = chars[i];
                }
                else
                {
                    countConsecutive++;
                }
            }
            if (chars.length > 0)
            {
                this.charMapList.add(new MapEntry<Character, Integer>(prevChar, countConsecutive));
            }
        }

        public void reset()
        {
             this.currentIndex = -1;
             this.charMapList.clear();
        }

        @Override
        public boolean hasMoreElements() {
            int totalCharLeft = 0;
            int i = charMapList.size() - 1;
            int minCharLeft = combinationCharCount - this.charPos;
            for (; i > this.currentIndex && totalCharLeft < minCharLeft; i--)
			{
			    totalCharLeft += charMapList.get(i).getValue();
			}
			return (totalCharLeft >= minCharLeft);
        }

        @Override
        public Character nextElement() throws NoSuchElementException {
            if (this.currentIndex + 1 >= this.charMapList.size())
            {
                throw new NoSuchElementException("No more elements");
            }
            return this.charMapList.get(++this.currentIndex).getKey();
        }

        public void setNextCombinationCharDomain(CombinationCharDomain combinationCharDomain)
        {
        	combinationCharDomain.reset();
            for (int i = this.currentIndex; i < this.charMapList.size(); i++)
            {
                if (i != this.currentIndex)
                {
                	combinationCharDomain.charMapList.add(this.charMapList.get(i));
                }
                else if (this.charMapList.get(i).getValue() > 1)
                {
                	combinationCharDomain.charMapList.add(new MapEntry<Character, Integer>(this.charMapList.get(i).getKey(), this.charMapList.get(i).getValue().intValue() - 1));
                }
            }
        }
    }

    @Override
    public boolean hasMoreElements() {
        // TODO Auto-generated method stub
        return (this.startChar > -1);
    }

    @Override
    public String nextElement() throws NoSuchElementException {
        // TODO Auto-generated method stub
        if (this.startChar > -1)
        {
        	int i = this.startChar;
            for (; i < this.combinationCharCount - 1; i++)
            {
                this.currentCombination.setCharAt(i, this.combinationCharDomains.get(i).nextElement());
                this.combinationCharDomains.get(i).setNextCombinationCharDomain(this.combinationCharDomains.get(i + 1));
            }
            this.currentCombination.setCharAt(i, this.combinationCharDomains.get(i).nextElement());

            // Finding startChar for next iteration
            this.startChar = this.combinationCharDomains.size() - 1;
            for (; this.startChar > -1 && !this.combinationCharDomains.get(this.startChar).hasMoreElements(); this.startChar--)
            {
            }

            return this.currentCombination.toString();
        }
        else
        {
            throw new NoSuchElementException("No more elements");
        }
    }

    public static void main (String [] args)
    {
        Enumeration<String> e = new Combinations("yellow", 4);

        while (e.hasMoreElements())
        {
             System.out.println(e.nextElement());
        }
    }
}