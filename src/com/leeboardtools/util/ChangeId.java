/*
 * Copyright 2014 Albert Santos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leeboardtools.util;

/**
 * This class provides a mechanism for asynchronously tracking changes to an object. The object
 * to be tracked maintains an instance of {@link ChangeId}. Each time a trackable change occurs, call
 * {@link #markChanged() }. This increments the id stored in {@link ChangeId }.
 * 
 * To track the changes, call {@link #newTracker() } to obtain a {@link Tracker} object.
 * {@link Tracker#isChanged() } or {@link Tracker#isChangedWithClean() } may then be called to determine
 * if the original {@link ChangeId} object has been marked changed. Once {@link Tracker#isChanged() } returns
 * {@code true} (indicating the ChangeId has been marked dirty), it will continue to return {@code true}
 * until {@link Tracker#clean() } is called. {@link Tracker#isChangedWithClean() } automatically calls {@link Tracker#clean() }.
 * 
 * Potential enhancement:
 * 	Add a Listener class, for synchronous notification of changes.
 * 
 * @author Albert Santos
 *
 */
public class ChangeId {
	private long mId;
	
	/**
	 * Basic constructor.
	 */
	public ChangeId() {
		mId = 0;
	}
	
	/**
	 * 
	 * @return	The current change id.
	 */
	public final long getId() {
		return mId;
	}

	/**
	 * Explicitly sets the change id. Normally markChanged() should be called instead of this.
	 * @param id Duh
	 */
	public void setId(long id) {
		mId = id;
	}
	
	/**
	 * Increments the change id.
	 */
	public void markChanged() {
		++mId;
	}
	
	
	/**
	 * Object used to track if a ChangeId has been modified.
	 * @author bertleft
	 *
	 */
	public class Tracker {
		private long mLastId;
		
		protected Tracker() {
			mLastId = mId;
		}
		
		/**
		 * 
		 * @return	The ChangeId object this is tracking (and that created this).
		 */
		public ChangeId getChangeId() {
			return ChangeId.this;
		}
		
		/**
		 * 
		 * @return	The id of the ChangeId object the last time this was cleaned or created.
		 */
		public final long getLastId() {
			return mLastId;
		}
		
		/**
		 * 
		 * @return	{@code true} if the ChangeId has been modified since this was last cleaned.
		 */
		public boolean isChanged() {
			return mId != mLastId;
		}
		
		/**
		 * Similar to isChanged(), except this immediately cleans itself.
		 * @return	{@code true} if the ChangeId has been modified since this was last cleaned.
		 */
		public boolean isChangedWithClean() {
			if (mId != mLastId) {
				mLastId = mId;
				return true;
			}
			return false;
		}
		
		/**
		 * Marks this as clean, isChanged() and isChangedWithClean() will return {@code false} until
		 * the ChangeId object's change Tracker is modified.
		 */
		public void clean() {
			mLastId = mId;
		}
		
		/**
		 * Forces {@link #isChanged()} to return {@code true}. 
		 */
		public void forceDirty() {
			mLastId = mId - 1;
		}
	}
	
	/**
	 * 
	 * @return	A Tracker object for tracking changes to the ChangeId.
	 */
	public Tracker newTracker() {
		return new Tracker();
	}
}
