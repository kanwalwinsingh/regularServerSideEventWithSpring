package org.kanwal.sse.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kanwal.sse.chat.ChatRoomEntry.ChatMessageEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

public class Spring4ChatRoomSSE implements chatRoomMessageListener {

	private static final Logger LOG  = LoggerFactory.getLogger(Spring4ChatRoomSSE.class);
	
	private List<SseEmitter> emitters = new ArrayList<>();
	protected final ChatRoomEntry chatRoom;

	@Override
	public void onPostMessage(ChatMessageEntry msg) {
		if(emitters.isEmpty()) {
			return;
		}
		SseEventBuilder eventBuilder = SseEmitter.event()
				.id(Integer.toString(msg.id))
				.name("chat")
				.data(msg);
		for(SseEmitter emitter : emitters) {
			try {
				emitter.send(eventBuilder);
			}catch (IOException e) {
				LOG.error("Failed to send msg to emitter", e);
			}
		}
	}
	
	public SseEmitter subscriber(String laseEventIdText) {
		SseEmitter emitter = new SseEmitter();
		Integer lastId = laseEventIdText!=null?Integer.parseInt(laseEventIdText):null;
		
		List<ChatMessageEntry> replyMsgs = (lastId!=null)?
				chatRoom.listMessageSinceLastId(lastId):chatRoom.listMessages();
				
		for(ChatMessageEntry msg: replyMsgs) {
			if(lastId!=null && msg.id<=lastId) {
				continue;
			}
			try {
				emitter.send(msg);
			}catch (IOException e) {
				LOG.error("Failed to re-send msg to emitter, ex:", e.getMessage() + " => complete with error ... remove,disconnect");
				emitter.completeWithError(e);
			}
		}
		emitters.add(emitter);
		
		emitter.onCompletion(()->{
			LOG.info("onCompletion ->remove emitter");
			emitters.remove(emitter);
		});
		
		emitter.onTimeout(()->{
			LOG.info("onTimeout ->remove emitter");
			emitters.remove(emitter);
		});
		return emitter;
	}
	

	public Spring4ChatRoomSSE(ChatRoomEntry chatRoom) {
		super();
		this.chatRoom = chatRoom;
	}
	
	
}
