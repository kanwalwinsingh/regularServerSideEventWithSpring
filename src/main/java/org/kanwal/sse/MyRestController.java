package org.kanwal.sse;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kanwal.sse.chat.ChatHistoryService;
import org.kanwal.sse.chat.ChatRoomEntry;
import org.kanwal.sse.chat.ChatRoomEntry.ChatMessageEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/app")
public class MyRestController {
	
	private static final Logger LOG = LoggerFactory.getLogger(MyRestController.class);

	@Autowired
	private ChatHistoryService chatHistoryService;
	
	
	@GetMapping("/helloParams")
		public Map<String, String> helloParams(){
			Map<String,String> res = new HashMap<>();
			res.put("hello", "World");
			return res;
		}
	
	@GetMapping("/health")
	public Map<String,String> health(){
		Map<String, String> res = new HashMap<>();
		res.put("status", "ok");
		return res;
	}
	
	public static class PostChatMesaage{
		public String msg;
		public String onBehalfOf;
	}
	
	@PostMapping(path="/chat/room/{chatRoom}")
	public void postChatMessage(@PathVariable("chatRoom")String chatRoom, @RequestBody PostChatMesaage msg) {
		ChatRoomEntry chatRoomEntry = chatHistoryService.getChatRoom(chatRoom);
	if(chatRoomEntry == null) {
		return;
	}
	String from = msg.onBehalfOf;
	if(from == null) {
		from = "From(cf security)";
	}
	LOG.info("recieve msg chatRoom:"+ chatRoom+ "from: "+from+"msg:"+msg.msg);
	chatRoomEntry.addMsg(new Date(), from, msg.msg);
	}

	@GetMapping(path="/chat/room/{chatRoom}/messages")
	public List<ChatMessageEntry> getChatMessages(@PathVariable("chatRoom")String chatRoom,
			@RequestParam(required = false) @DateTimeFormat(iso=ISO.DATE) Date since,
			@RequestParam(required= false, defaultValue="200")int limit){
		ChatRoomEntry chatRoomEntry = chatHistoryService.getChatRoom(chatRoom);
		if(chatRoomEntry==null) {
			return Collections.emptyList();
		}
		return chatRoomEntry.listMessageSince(since);
	}
	
	@GetMapping(path="/chat/room/{chatRoom}/subscribeMessagesSpring4", produces="text/event-stream")
	public SseEmitter subscribeChatMessages(
			@PathVariable("chatRoom") String chatRoom,
			@RequestHeader(name="last-event-id",required=false)String lastEventId) {
		ChatRoomEntry chatRoomEntry = chatHistoryService.getChatRoom(chatRoom);
		if(chatRoomEntry == null) {
			return null;
		}
		LOG.info("subscribeMessagesSpring4 lastEventId:" + lastEventId);
		return chatRoomEntry.subscribeSpring4(lastEventId);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
