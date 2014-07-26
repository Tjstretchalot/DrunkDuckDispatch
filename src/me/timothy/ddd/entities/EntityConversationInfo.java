/* DrunkDuckDispatch Copyright (C) 2014  Timothy Moore
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.timothy.ddd.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class EntityConversationInfo extends JSONCompatible {
	public static final int NO_JUMP_TO = Integer.MIN_VALUE;
	public class ConversationMessage extends JSONCompatible {
		private String msg;
		private int jumpTo;
		private boolean end;
		private List<ConversationChoice> choices;
		
		public ConversationMessage() {
			jumpTo = NO_JUMP_TO;
		}
		
		@Override
		public void loadFrom(JSONObject jsonObject) {
			Set<?> keys = jsonObject.keySet();

			for(Object o : keys) {
				if(o instanceof String) {
					String key = (String) o;
					switch(key.toLowerCase()) {
					case "msg":
						msg = getString(jsonObject, key);
						break;
					case "choices":
						choices = new ArrayList<>();
						JSONArray arr = getArray(jsonObject, key);
						for(Object o2 : arr) {
							if(o2 instanceof JSONObject) {
								ConversationChoice cc = new ConversationChoice();
								cc.loadFrom((JSONObject) o2);
								choices.add(cc);
							}
						}
						break;
					case "jump-to":
						jumpTo = getInt(jsonObject, key);
						break;
					case "end":
						end = getBoolean(jsonObject, key);
						break;
					default:
						logger.warn("Unknown key in ConversationMessage: " + key);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void saveTo(JSONObject jsonObject) {
			jsonObject.put("msg", msg);
			jsonObject.put("jump-to", jumpTo);
			jsonObject.put("end", end);
			if(choices != null) {
				JSONArray arr = new JSONArray();
				for(ConversationChoice cc : choices) {
					JSONObject jObj = new JSONObject();
					cc.saveTo(jObj);
					arr.add(jObj);
				}
				jsonObject.put("choices", arr);
			}
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
		
		public int getJumpTo() {
			return jumpTo;
		}
		
		public void setJumpTo(int jumpTo) {
			this.jumpTo = jumpTo;
		}
		
		public boolean isEnd() {
			return end;
		}
		
		public void setEnd(boolean end) {
			this.end = end;
		}
		
		public List<ConversationChoice> getChoices() {
			return choices;
		}

		public void setChoices(List<ConversationChoice> choices) {
			this.choices = choices;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (end ? 1231 : 1237);
			result = prime * result
					+ ((choices == null) ? 0 : choices.hashCode());
			result = prime * result + ((msg == null) ? 0 : msg.hashCode());
			result = prime * result + jumpTo;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConversationMessage other = (ConversationMessage) obj;
			if(jumpTo != other.jumpTo)
				return false;
			if(end != other.end)
				return false;
			if (choices == null) {
				if (other.choices != null)
					return false;
			} else if (!choices.equals(other.choices))
				return false;
			if (msg == null) {
				if (other.msg != null)
					return false;
			} else if (!msg.equals(other.msg))
				return false;
			
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConversationMessage [");
			if (msg != null)
				builder.append("msg=").append(msg).append(", ");
			builder.append("jumpTo=").append(jumpTo).append(", end=")
					.append(end);
			if (choices != null)
				builder.append(", ").append("choices=").append(choices);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	public class ConversationChoice extends JSONCompatible {
		private String msg;
		private int jumpTo;
		private boolean end;
		
		public ConversationChoice() {
			jumpTo = NO_JUMP_TO;
		}
		
		@Override
		public void loadFrom(JSONObject jsonObject) {
			Set<?> keys = jsonObject.keySet();

			for(Object o : keys) {
				if(o instanceof String) {
					String key = (String) o;
					switch(key.toLowerCase()) {
					case "msg":
						msg = getString(jsonObject, key);
						break;
					case "jump-to":
						jumpTo = getInt(jsonObject, key);
						break;
					case "end":
						end = getBoolean(jsonObject, key);
						break;
					default:
						logger.warn("Unknown key in ConversationChoice: " + key);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void saveTo(JSONObject jsonObject) {
			jsonObject.put("msg", msg);
			if(jumpTo != NO_JUMP_TO)
				jsonObject.put("jump-to", jumpTo);
			jsonObject.put("end", end);
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public int getJumpTo() {
			return jumpTo;
		}

		public void setJumpTo(int jumpTo) {
			this.jumpTo = jumpTo;
		}

		public boolean isEnd() {
			return end;
		}

		public void setEnd(boolean end) {
			this.end = end;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (end ? 1231 : 1237);
			result = prime * result + jumpTo;
			result = prime * result + ((msg == null) ? 0 : msg.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConversationChoice other = (ConversationChoice) obj;
			if (end != other.end)
				return false;
			if (jumpTo != other.jumpTo)
				return false;
			if (msg == null) {
				if (other.msg != null)
					return false;
			} else if (!msg.equals(other.msg))
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConversationChoice [");
			if (msg != null)
				builder.append("msg=").append(msg).append(", ");
			builder.append("jumpTo=").append(jumpTo).append(", end=")
					.append(end).append("]");
			return builder.toString();
		}
		
	}
	
	private Logger logger;
	private List<ConversationMessage> text;
	private String quest;
	
	public EntityConversationInfo() {
		logger = LogManager.getLogger();
	}
	
	@Override
	public void loadFrom(JSONObject jsonObject) {
		Set<?> keys = jsonObject.keySet();

		for(Object o : keys) {
			if(o instanceof String) {
				String key = (String) o;
				switch(key.toLowerCase()) {
				case "text":
					JSONArray arr = getArray(jsonObject, key);
					text = new ArrayList<>();
					for(Object o2 : arr) {
						if(o2 instanceof JSONObject) {
							ConversationMessage cm = new ConversationMessage();
							cm.loadFrom((JSONObject) o2);
							text.add(cm);
						}
					}
					break;
				case "quest":
					quest = getString(jsonObject, key);
					break;
				default:
					logger.warn("Unknown key: " + key);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveTo(JSONObject jsonObject) {
		if(text != null) {
			JSONArray arr = new JSONArray();
			for(ConversationMessage cm : text) {
				JSONObject jObj = new JSONObject();
				cm.saveTo(jObj);
				arr.add(jObj);
			}
			jsonObject.put("text", arr);
		}
		jsonObject.put("quest", quest);
	}

	public List<ConversationMessage> getText() {
		return text;
	}

	public void setText(List<ConversationMessage> text) {
		this.text = text;
	}

	public String getQuest() {
		return quest;
	}

	public void setQuest(String quest) {
		this.quest = quest;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((quest == null) ? 0 : quest.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityConversationInfo other = (EntityConversationInfo) obj;
		if (quest == null) {
			if (other.quest != null)
				return false;
		} else if (!quest.equals(other.quest))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EntityConversationInfo [");
		if (text != null)
			builder.append("text=").append(text).append(", ");
		if (quest != null)
			builder.append("quest=").append(quest);
		builder.append("]");
		return builder.toString();
	}

}
