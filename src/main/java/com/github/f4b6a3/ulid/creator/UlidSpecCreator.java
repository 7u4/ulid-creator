/*
 * MIT License
 * 
 * Copyright (c) 2020 Fabio Lima
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

package com.github.f4b6a3.ulid.creator;

import java.util.Random;
import java.util.UUID;

import com.github.f4b6a3.ulid.strategy.RandomStrategy;
import com.github.f4b6a3.ulid.strategy.random.DefaultRandomStrategy;
import com.github.f4b6a3.ulid.strategy.random.OtherRandomStrategy;
import com.github.f4b6a3.ulid.strategy.TimestampStrategy;
import com.github.f4b6a3.ulid.strategy.timestamp.DefaultTimestampStrategy;
import com.github.f4b6a3.ulid.util.internal.UlidStruct;

/**
 * Factory that creates lexicographically sortable GUIDs, based on the ULID
 * specification - Universally Unique Lexicographically Sortable Identifier.
 * 
 * ULID specification: https://github.com/ulid/spec
 */
public class UlidSpecCreator {

	protected long random1 = 0;
	protected long random2 = 0;

	protected long randomMax1;
	protected long randomMax2;

	protected static final long HALF_RANDOM_COMPONENT = 0x000000ffffffffffL;
	protected static final long INCREMENT_MAX = 0x0000010000000000L;

	protected long previousTimestamp;

	protected static final String OVERRUN_MESSAGE = "The system overran the generator by requesting too many ULIDs.";

	protected TimestampStrategy timestampStrategy;
	protected RandomStrategy randomStrategy;

	public UlidSpecCreator() {
		this.timestampStrategy = new DefaultTimestampStrategy();
		this.randomStrategy = new DefaultRandomStrategy();
	}

	/**
	 * 
	 * Return a GUID based on the ULID specification.
	 * 
	 * A ULID has two parts:
	 * 
	 * 1. A part of 48 bits that represent the amount of milliseconds since Unix
	 * Epoch, 1 January 1970.
	 * 
	 * 2. A part of 80 bits that has a random value generated a secure random
	 * generator.
	 * 
	 * The random part is reset to a new value every time the millisecond part
	 * changes.
	 * 
	 * If more than one GUID is generated within the same millisecond, the random
	 * part is incremented by one.
	 * 
	 * The maximum GUIDs that can be generated per millisecond is 2^80.
	 * 
	 * The random part is generated by a secure random number generator:
	 * {@link java.security.SecureRandom}.
	 * 
	 * ### Specification of Universally Unique Lexicographically Sortable ID
	 * 
	 * #### Components
	 * 
	 * ##### Timestamp
	 * 
	 * It is a 48 bit integer. UNIX-time in milliseconds. Won't run out of space
	 * 'til the year 10889 AD.
	 * 
	 * ##### Randomness
	 * 
	 * It is a 80 bits integer. Cryptographically secure source of randomness, if
	 * possible.
	 * 
	 * #### Sorting
	 * 
	 * The left-most character must be sorted first, and the right-most character
	 * sorted last (lexical order). The default ASCII character set must be used.
	 * Within the same millisecond, sort order is not guaranteed.
	 * 
	 * #### Monotonicity
	 * 
	 * When generating a ULID within the same millisecond, we can provide some
	 * guarantees regarding sort order. Namely, if the same millisecond is detected,
	 * the random component is incremented by 1 bit in the least significant bit
	 * position (with carrying).
	 * 
	 * If, in the extremely unlikely event that, you manage to generate more than
	 * 2^80 ULIDs within the same millisecond, or cause the random component to
	 * overflow with less, the generation will fail.
	 * 
	 * @return {@link UUID} a GUID value
	 */
	public synchronized UUID create() {
		return UlidStruct.of(this.getTimestamp(), random1, random2).toUuid();
	}

	/**
	 * 
	 * Return a GUID based on the ULID specification.
	 * 
	 * It is compatible with the RFC-4122 UUID v4.
	 * 
	 * @return {@link UUID} a GUID value
	 */
	public synchronized UUID create4() {
		return UlidStruct.of(this.getTimestamp(), random1, random2).toUuid4();
	}

	/**
	 * Returns a ULID string.
	 * 
	 * The returning string is encoded to Crockford's base32.
	 * 
	 * The random component is generated by a secure random number generator:
	 * {@link java.security.SecureRandom}.
	 * 
	 * @return a ULID string
	 */
	public synchronized String createString() {
		return UlidStruct.of(this.getTimestamp(), random1, random2).toString();
	}

	/**
	 * Returns a ULID string.
	 * 
	 * It is compatible with the RFC-4122 UUID v4.
	 * 
	 * The returning string is encoded to Crockford's base32.
	 * 
	 * The random component is generated by a secure random number generator:
	 * {@link java.security.SecureRandom}.
	 * 
	 * @return a ULID string
	 */
	public synchronized String createString4() {
		return UlidStruct.of(this.getTimestamp(), random1, random2).toString4();
	}

	/**
	 * Return the current timestamp and resets or increments the random part.
	 * 
	 * @return timestamp
	 */
	protected synchronized long getTimestamp() {

		final long timestamp = this.timestampStrategy.getTimestamp();

		if (timestamp == this.previousTimestamp) {
			this.increment();
		} else {
			this.reset();
		}

		this.previousTimestamp = timestamp;
		return timestamp;
	}

	/**
	 * Reset the random part of the GUID.
	 */
	protected synchronized void reset() {

		// Get random values
		final byte[] bytes = new byte[10];
		this.randomStrategy.nextBytes(bytes);
		
		this.random1 = (long) (bytes[0x0] & 0xff) << 32;
		this.random1 |= (long) (bytes[0x1] & 0xff) << 24;
		this.random1 |= (long) (bytes[0x2] & 0xff) << 16;
		this.random1 |= (long) (bytes[0x3] & 0xff) << 8;
		this.random1 |= (long) (bytes[0x4] & 0xff);

		this.random2 = (long) (bytes[0x5] & 0xff) << 32;
		this.random2 |= (long) (bytes[0x6] & 0xff) << 24;
		this.random2 |= (long) (bytes[0x7] & 0xff) << 16;
		this.random2 |= (long) (bytes[0x8] & 0xff) << 8;
		this.random2 |= (long) (bytes[0x9] & 0xff);
		
		// Save the random values
		this.randomMax1 = this.random1 | INCREMENT_MAX;
		this.randomMax2 = this.random2 | INCREMENT_MAX;
	}

	/**
	 * Increment the random part of the GUID.
	 */
	protected synchronized void increment() {
		if (++this.random2 >= this.randomMax2) {
			this.random2 = this.random2 & HALF_RANDOM_COMPONENT;
			if ((++this.random1 >= this.randomMax1)) {
				this.reset();
			}
		}
	}

	/**
	 * Used for changing the timestamp strategy.
	 * 
	 * @param timestampStrategy a timestamp strategy
	 * @return {@link UlidSpecCreator}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends UlidSpecCreator> T withTimestampStrategy(TimestampStrategy timestampStrategy) {
		this.timestampStrategy = timestampStrategy;
		return (T) this;
	}

	/**
	 * Replaces the default random strategy with another.
	 * 
	 * The default random strategy uses {@link java.security.SecureRandom}.
	 * 
	 * See {@link Random}.
	 * 
	 * @param random a random generator
	 * @param <T>    the type parameter
	 * @return {@link AbstractRandomBasedUuidCreator}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends UlidSpecCreator> T withRandomStrategy(RandomStrategy randomStrategy) {
		this.randomStrategy = randomStrategy;
		return (T) this;
	}

	/**
	 * Replaces the default random strategy with another that uses the input
	 * {@link Random} instance.
	 * 
	 * It replaces the internal {@link DefaultRandomStrategy} with
	 * {@link OtherRandomStrategy}.
	 * 
	 * @param random a random generator
	 * @return {@link UlidSpecCreator}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends UlidSpecCreator> T withRandomGenerator(Random random) {
		this.randomStrategy = new OtherRandomStrategy(random);
		return (T) this;
	}

	/**
	 * For unit tests
	 */
	protected long extractRandom1(UUID uuid) {
		return ((uuid.getMostSignificantBits() & 0x000000000000ffff) << 24) | (uuid.getLeastSignificantBits() >>> 40);
	}

	/**
	 * For unit tests
	 */
	protected long extractRandom2(UUID uuid) {
		return uuid.getLeastSignificantBits() & HALF_RANDOM_COMPONENT;
	}
}
