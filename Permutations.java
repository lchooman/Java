package com.android.lewis.permutations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;

/*
    A Java Enumeration implementation to return permutations of characters in any string (at least one-character long), without any duplication caused by repeated characters.
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

public class Permutations implements Enumeration<String> {

    private List<PermutationCharDomain> permutationCharDomains = new ArrayList<PermutationCharDomain>();
    private int permutationCharCount;
    private int startChar = 0;
    private StringBuilder currentPermutation;

    public Permutations (String s, int permutationCharCount)
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
		else if (permutationCharCount <= 0)
		{
            throw new IllegalArgumentException("Permutation character count cannot be zero or less");
		}
		else if (permutationCharCount > sLen)
		{
            throw new IllegalArgumentException("Permutation character count cannot be higher than he length of domain string");
        }

        this.permutationCharDomains.add(new PermutationCharDomain(s));
        for (int i = 1; i < permutationCharCount; i++)
        {
            this.permutationCharDomains.add(new PermutationCharDomain());
        }
        this.permutationCharCount = permutationCharCount;
        this.currentPermutation = new StringBuilder(s.substring(0, permutationCharCount));
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
    
    private class PermutationCharDomain implements Enumeration<Character> {
        private int currentIndex = -1;
        private List<Map.Entry<Character, Integer>> charMapList = new ArrayList<Map.Entry<Character, Integer>>();

        public PermutationCharDomain()
        {
        }
        
        public PermutationCharDomain(String s)
        {
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
            return ((this.currentIndex + 1) < charMapList.size());
        }

        @Override
        public Character nextElement() throws NoSuchElementException {
            if (this.currentIndex + 1 >= this.charMapList.size())
            {
                throw new NoSuchElementException("No more elements");
            }
            return this.charMapList.get(++this.currentIndex).getKey();
        }

        public void setNextPermutationCharDomain(PermutationCharDomain permutationCharDomain)
        {
        	permutationCharDomain.reset();
            for (int i = 0; i < this.charMapList.size(); i++)
            {
                if (i != this.currentIndex)
                {
                	permutationCharDomain.charMapList.add(this.charMapList.get(i));
                }
                else if (this.charMapList.get(i).getValue() > 1)
                {
                	permutationCharDomain.charMapList.add(new MapEntry<Character, Integer>(this.charMapList.get(i).getKey(), this.charMapList.get(i).getValue().intValue() - 1));
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
            for (; i < this.permutationCharCount - 1; i++)
            {
                this.currentPermutation.setCharAt(i, this.permutationCharDomains.get(i).nextElement());
                this.permutationCharDomains.get(i).setNextPermutationCharDomain(this.permutationCharDomains.get(i + 1));
            }
            this.currentPermutation.setCharAt(i, this.permutationCharDomains.get(i).nextElement());

            // Finding startChar for next iteration
            this.startChar = this.permutationCharDomains.size() - 1;
            for (; this.startChar > -1 && !this.permutationCharDomains.get(this.startChar).hasMoreElements(); this.startChar--)
            {
            }

            return this.currentPermutation.toString();
        }
        else
        {
            throw new NoSuchElementException("No more elements");
        }
    }

    public static void main (String [] args)
    {
        Enumeration<String> e = new Permutations("yellow", 4);

        while (e.hasMoreElements())
        {
             System.out.println(e.nextElement());
        }
    }
}