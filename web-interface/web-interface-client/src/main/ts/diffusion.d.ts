
export interface Diffusion {
  connect: (options: Options) => Result;
  log: (level: string) => void;
  datatypes;
  selectors;
  metadata;
  topics;
  errorReport;
  clients;
}

export interface ErrorReport {
  message: string;
  line: number;
  column: number;
}

export interface Error {
  id: number;
  message: string;
  canReconnect: boolean;
}

export interface Options {
  host?: string,
  port?: number,
  secure?: boolean,
  principal?: string,
  credentials?: string
}




export interface Session extends Topics {
  close: () => Session;
  isConnected: () => boolean;
  isClosed: () => boolean;
  security;
  topics: TopicControl;
  messages;
  clients;
  toString: () => string;
}



export interface BinaryDataType extends DataType {
  from: (buffer) => BinaryDataType;
}

export interface DataType {
  name: string;
  readValue: (input, offset: number, length: number) => any;
  writeValue: (value) => any;
  deltaType: (name: string, delta) => any;
}





export interface DataTypes {
  binary: () => BinaryDataType;
  json: () => JsonDataType;
  get: () => DataType;
}



export interface JsonDataType extends DataType {
  from: (object) => JsonDataType;
  fromJsonString: (str: string) => JsonDataType;
}

export interface FetchStream {

}




export interface Result {
  then: (fulfilled: (value: any) => void, rejected: (error: Error) => void) => Result;
}





export interface Subscription {
  selector: string;
  asType: (type: DataType) => TypedSubscription;
  view: View;
  transform: (transformer) => Subscription;
  close: () => void;
}



export interface TypedSubscription extends Subscription {

}

export interface View {

}



export interface MessageHandler {
  onMessage: (message: SessionMessage) => void;
  onActive: (unregister: any) => void;
  onClose: () => void;
}

export interface UpdateSourceHandler {
  onRegistered?: (topicPath?: string, unregister?: any) => void;
  onActive?: (topicPath?: string, updater?: any) => void;
  onStandby?: (topicPath?: string) => void;
  onClose?: (topicPath?: string) => void;
}




export interface ClientControl {
  subscribe: (session: string, selector: string) => Result;
  unsubscribe: (session: string, selector: string) => Result;
  getSessionProperties: (sessionID: string, requiredProperties: string[]) => Result;
  setSessionPropertiesListener: (requiredProperties: string[], listener: SessionPropertiesListener) => Result;
  SessionEventType: number;
}

export interface SessionPropertiesListener {
  onActive: (deregister: any) => void;
  onClose: () => void;
  onSessionOpen: (session: Session, properties: any) => void;
  onSessionEvent: (session: string, SessionEventType: number, properties: any, previous: any) => void;
  onSessionClose: (session: string, properties: any, reason: any) => void;
}




export interface Messages {
  send: (path: string, message: any, sessionID: string) => Result;
  listen: (path: string, listener: any) => MessageStream;
  addHandler: (path: string, handler: MessageHandler) => Result;

}

export interface MessageStream extends Stream {

}

export interface Stream {

}

export interface SessionMessage {

}




export interface TopicControl {
  add: (path: string, supplied?: any, initial?: any) => Result;
  remove: (path: string) => Result;
  removeWithSession: (topicPath: string) => Result;
  update: (path: string, value: any) => Result;
  registerUpdateSource: (topicPath: string, handler: UpdateSourceHandler) => any;
}

export interface Updater {
  update: (topicPath: string, value: any) => Result
}






export interface Topics {
  subscribe: (selector: string, callback?) => Subscription;
  unsubscribe: (selector: string) => Result;
  stream: (selector: string, callback?) => Subscription;
  view: (selector: string, callback?) => View;
  fetch: (selector: string) => FetchStream;
}

export interface Metadata {
  String: String;
  Integer: Integer;
  Decimal: Decimal;
  Stateless: Stateless;
  RecordContent: RecordContent;
}

export interface String {
  new(str?: string): String;
  value: string;
}

export interface Integer {
  new(num?: number): Integer;
  value: number;
}

export interface Decimal {
  new(num?: number, scale?: number): Decimal;
  value: number;
  scale: number;
}

export interface Stateless {
  new(): Stateless;
}

export interface RecordContent {
  new(): RecordContent;
  occurs: (min: number, max?: number) => Occurs;
  addRecord: (name: string, occurs?: any, fields?: any) => Record;
  getRecord: (key: number) => Record;
  getRecords: () => Record[];
  string: () => String;
  integer: () => Integer;
  decimal: () => Decimal;
  builder: () => RecordContentBuilder;
  parse: (buffer: any) => RecordContent;
}

export interface RecordContentBuilder {
  add: () => Record;
  build: () => RecordContent;
}

export interface Record {
  name: string;
  occurs: Occurs;
  addField: (name: string, type: any, occurs: any) => Field;
  getField: (key: number) => Field;
  getFields: () => Field[];
}

export interface Field {
  name: string;
  type: String|Integer|Decimal;
  occurs: Occurs;
}

export interface Occurs {
  min: number;
  max: number;
}

export interface TopicSelector {
  type: string;
  prefix: string;
  expression: string;
  selects: (topicPath: string) => boolean;
  toString: () => string;
}



export interface TopicSelectors {
  parse: (...expression: string[]) => TopicSelector;
}
