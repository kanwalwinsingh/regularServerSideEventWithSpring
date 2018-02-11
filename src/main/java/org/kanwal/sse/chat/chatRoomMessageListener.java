package org.kanwal.sse.chat;

import org.kanwal.sse.chat.ChatRoomEntry.ChatMessageEntry;

public interface chatRoomMessageListener {

	public void onPostMessage(ChatMessageEntry msg);
}
