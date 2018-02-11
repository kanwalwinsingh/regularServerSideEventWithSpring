package org.kanwal.sse.chat;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class ChatRoomEntry {
	
	public static class ChatMessageEntry{
		
		public final int id;
		public final Date date;
		public final String from;
		public final String chatRoom;
		public final String msg;
		public ChatMessageEntry(int id, Date date, String from, String chatRoom, String msg) {
			super();
			this.id = id;
			this.date = date;
			this.from = from;
			this.chatRoom = chatRoom;
			this.msg = msg;
		}
		
		@Override
		public String toString() {
			return "ChatMessage[" + date + ", from=" + from + ", room=" + chatRoom + " : " + msg + "]";
		}
	}

	public final String name;
	private int idGenerator = 1;
	private List<ChatMessageEntry> messages = new ArrayList<>();
	
	private int maxRecentHistory = 20;
	
	private List<chatRoomMessageListener> listeners = new ArrayList<>();
	private Spring4ChatRoomSSE spring4ChatRoomSSE;
	
	public ChatRoomEntry(String name) {
		this.name = name;
		this.spring4ChatRoomSSE = new Spring4ChatRoomSSE(this);
		listeners.add(spring4ChatRoomSSE);
		
	}
	
	public void addMsg(Date date, String from, String msg) {
		int id = idGenerator++;
		ChatMessageEntry msgEntry = new ChatMessageEntry(id, date, from, name, msg);
		messages.add(msgEntry);
		if(messages.size()>maxRecentHistory) {
			messages.remove(0);
		}
		for(chatRoomMessageListener listener:listeners) {
			listener.onPostMessage(msgEntry);
		}
	}
	
	public List<ChatMessageEntry> listMessageSince(Date since){
		return messages.stream().filter(x->since==null||x.date.after(since))
				.collect(Collectors.toList());
	}
	
	public List<ChatMessageEntry> listMessageSinceLastId(int lastId){
		return messages.stream().filter(x->x.id>lastId).collect(Collectors.toList());
	}
	
	public List<ChatMessageEntry> listMessages(){
		return new ArrayList<>(messages);
	}
	
	public SseEmitter subscribeSpring4(String lastEventId) {
		return spring4ChatRoomSSE.subscriber(lastEventId);
	}
}
