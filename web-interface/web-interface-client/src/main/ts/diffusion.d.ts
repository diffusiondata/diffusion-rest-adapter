declare module "diffusion" {
    type LogVerbosityLevel = "silent" | "error" | "warn" | "info" | "debug";
    export var version: string;
    export function connect(options: SessionOptions): Result < Session, ErrorReason > ;
    export function log(level: LogVerbosityLevel): void;
    export var datatypes: DataTypes;
    export var selectors: TopicSelectors;
    export var metadata: Metadata;
    export var topics: {
        TopicType: {
            JSON: TopicType;
            BINARY: TopicType;
            RECORD: TopicType;
            SINGLE_VALUE: TopicType;
            STRING: TopicType;
            INT64: TopicType;
            DOBULE: TopicType;
        };
        UnsubscribeReason: {
            REQUESTED: any;
            CONTROL: any;
            REMOVED: any;
            AUTHORIZATION: any;
        };
        TopicAddFailReason: {
            EXISTS: TopicAddFailReason,
            EXISTS_MISMATCH: TopicAddFailReason,
            INVALID_PATH: TopicAddFailReason,
            INVALID_DETAILS: TopicAddFailReason,
            USER_CODE_ERROR: TopicAddFailReason,
            TOPIC_NOT_FOUND: TopicAddFailReason,
            PERMISSIONS_FAILURE: TopicAddFailReason,
            INITIALISE_ERROR: TopicAddFailReason,
            UNEXPECTED_ERROR: TopicAddFailReason,
            CLUSTER_REPARTITION: TopicAddFailReason
        };
    };
    export var errorReport: ErrorReport;
    export var clients: {
        PropertyKeys: {
            ALL_FIXED_PROPERTIES: any;
            ALL_USER_PROPERTIES: any;
            ALL_PROPERTIES: any;
        },
        CloseReason: {
            CLOSED_BY_CLIENT: CloseReason,
            CLOSED_BY_SERVER: CloseReason,
            RECONNECT_ABORTED: CloseReason,
            CONNECTION_TIMEOUT: CloseReason,
            HANDSHAKE_REJECTED: CloseReason,
            HANDSHAKE_ERROR: CloseReason,
            TRANSPORT_ERROR: CloseReason,
            CONNECTION_ERROR: CloseReason,
            IDLE_CONNECTED: CloseReason,
            LOST_MESSAGES: CloseReason,
            ACCESS_DENIED: CloseReason
        }
    };
    export interface ErrorReport {
        message: string;
        line: number;
        column: number;
    }
    export interface ErrorReason {
        id: number;
        message: string;
        canReconnect: boolean;
    }
    export type TransportType = "ws" | "WS" | "WEBSOCKET" | "xhr" | "XHR" | "HTTP_POLLING";
    export interface SessionOptions {
        host ? : string;
        port ? : number;
        path ? : string;
        secure ? : boolean;
        principal ? : string;
        credentials ? : string;
        reconnect ? : boolean | number | Function | ReconnectOptions;
        transports ? : TransportType | TransportType[];
    }
    export interface ReconnectOptions {
        timeout: number;
        strategy: Function;
    }
    export interface Session extends Topics, Ping, Stream {
        close: () => Session;
        isConnected: () => boolean;
        isClosed: () => boolean;
        security: Security;
        topics: TopicControl;
        timeseries: TimeSeries;
        messages: Messages;
        notifications: Notifications;
        clients: ClientControl;
        options: SessionOptions;
        sessionID: string;
        toString: () => string;
        stream: (selector ? : string, callback ? : Function) => Subscription;
    }
    export interface TopicType {
        id: number;
        stateful: boolean;
        functional: boolean;
    }
    export interface CloseReason {
        id: number,
            message: string
    }
    export interface BinaryDataType extends DataType < Binary > {
        Binary: Binary;
    }
    export interface Binary extends Bytes {
        apply: (delta: any) => Binary;
        diff: (original: any, type: string) => BinaryDelta;
        get: () => any;
    }
    export interface BinaryDelta {
        hasChanges: () => boolean;
    }
    export interface Bytes {
        length: () => number;
        asBuffer: () => any;
        copyTo: (target, offset ? : number) => void;
    }
    export interface DataType < T > {
        from: (buffer) => T;
        name: string;
        readValue: (input: any, offset ? : number, length ? : number) => any;
        writeValue: (value) => any;
        deltaType: (name: string, delta) => any;
        canReadAs: < V > (valueClass) => V;
        readAs: < V > (valueClass, buffer, offset ? : number, length ? : number) => V;
    }
    export interface DataTypes {
        binary: () => BinaryDataType;
        json: () => JsonDataType;
        string: () => StringDataType;
        double: () => DoubleDataType;
        int64: () => Int64DataType;
        recordv2: () => RecordV2DataType;
        get: (name: any) => JsonDataType | BinaryDataType | StringDataType | DoubleDataType | Int64DataType | RecordV2DataType;
    }
    export interface DoubleDataType extends DataType < number > {}
    export interface Int64DataType extends DataType < Int64 > {
        Int64: Int64;
    }
    export interface Int64 {
        toString: (radix ? : number) => string;
        toNumber: () => number;
    }
    export interface JsonDataType extends DataType < Json > {
        fromJsonString: (str: string) => Json;
        JSON: Json;
    }
    export interface Json extends Bytes {
        apply: (delta: any) => Json;
        diff: (original: any, type: string) => JsonDelta;
        get: () => any;
    }
    export interface JsonDelta {
        hasChanges: () => boolean;
    }
    export interface RecordV2Delta {
        changes: (schema: Schema) => Change[];
    }
    export interface Change {
        recordName: string;
        recordIndex: number;
        fieldName: string;
        fieldIndex: number;
        key: string;
        type: string;
    }
    export interface RecordModel {
        get: (recordName: string, recordIndex: number, fieldName: String, fieldIndex: number) => string;
        asValue: () => RecordV2;
        fieldCount: (recordName: string, recordIndex: number, fieldName: string) => number;
        recordCount: (recordName: string) => number;
    }
    export interface MutableRecordModel extends RecordModel {
        set: (recordName: string, recordIndex: number, fieldName: string, fieldIndex: number, value: string) => MutableRecordModel;
        add: (recordName: string, recordIndex: number, ...values: string[]) => MutableRecordModel;
        addRecord: () => MutableRecordModel;
        removeRecord: (index: number) => MutableRecordModel;
        removeField: (recordName: string, recordIndex: number, fieldIndex: number) => MutableRecordModel;
    }
    export interface Node {
        name: string;
        min: number;
        max: number;
        isVariable: boolean;
    }
    export interface Record extends Node {
        fields: Field[];
    }
    export interface Field extends Node {
        scale ? : number
    }
    export interface Schema {
        getRecords: () => Record[];
        asJSON: () => any;
        createMutableModel: () => MutableRecordModel;
    }
    export interface RecordV2 extends Bytes {
        diff: (original: RecordV2) => RecordV2Delta;
        asModel: (schema: Schema) => RecordModel;
        asValidatedModel: (schema: Schema) => RecordModel;
        asRecords: () => string[][];
        asFields: () => string[];
    }
    export interface SchemaBuilder {
        record: (name: string, min ? : number, max ? : number) => SchemaBuilder;
        string: (name: string, min ? : number, max ? : number) => SchemaBuilder;
        integer: (name: string, min ? : number, max ? : number) => SchemaBuilder;
        decimal: (name: string, scale: number, min ? : number, max ? : number) => SchemaBuilder;
        build: () => Schema
    }
    export interface RecordV2Builder {
        addFields: (values: string[]) => RecordV2Builder;
        addRecord: (fields: string[]) => RecordV2Builder;
        clear: () => RecordV2Builder;
        build: () => RecordV2;
    }
    export interface RecordV2DataType extends DataType < RecordV2 > {
        withSchema: (schema: Schema) => RecordV2DataType;
        parseSchema: (string) => Schema;
        valueBuilder: () => RecordV2Builder;
        schemaBuilder: () => SchemaBuilder;
    }
    export interface StringDataType extends DataType < string > {}
    export interface FetchStream extends Stream {}
    export interface Result < T, E > {
        then < TResult,
        TE > (fulfilled: (value: T) => TResult | Result < TResult, TE > , rejected ? : (reason: any) => void): Result < TResult,
        E > ;
    }
    export interface Stream {
        on: (events: string | Object, listener ? : Function) => Stream;
        off: (event: string, listener: Function) => Stream;
        close: () => void;
    }
    export interface Subscription extends Stream {
        selector: string;
        view: View;
        transform(transformer): Subscription;
        asType < T > (type: DataType < T > ): ValueStream;
    }
    /**
     * Provides a stream of topic events. This is an alias for {@link ValueStream}.
     * <P>
     * This is deprecated in favor of {@link ValueStream}.
     *
     * @deprecated since 6.0
     */
    export type TypedSubscription = ValueStream;
    export interface ValueStream extends Subscription {}
    export interface View extends Stream {
        get: () => any;
    }
    export interface ClientControl {
        close: (sessionID: string) => Result < void, any > ;
        subscribe: (session: string, selector: string) => Result < number, any > ;
        unsubscribe: (session: string, selector: string) => Result < number, any > ;
        getSessionProperties: (sessionID: string | Object, requiredProperties: string[]) => Result < SessionProperties, any > ;
        setSessionPropertiesListener: (requiredProperties: string[], listener: SessionPropertiesListener) => Result < void, any > ;
        setSessionProperties: (sessionID: string | Object, sessionProperties: SessionProperties) => Result < SessionProperties, any > ;
        setSessionPropertiesByFilter: (filter: string, sessionProperties: SessionProperties) => Result < void, any > ;
        SessionEventType: {
            UPDATED: number;
            RECONNECTED: number;
            FAILED_OVER: number;
            DISCONNECTED: number;
        };
    }
    export interface SessionPropertiesListener {
        onActive: (deregister: any) => void;
        onClose: () => void;
        onSessionOpen: (session: Object, properties: any) => void;
        onSessionEvent: (session: Object, SessionEventType: number, properties: any, previous: any) => void;
        onSessionClose: (session: Object, properties: any, reason: any) => void;
    }
    export interface SessionProperties {
        [key: string]: string;
    }
    export interface Messages {
        send(path: string, message: any, sessionID ? : string | Object): Result < SendResult, any > ;
        listen(path: string, listener ? : (message: SessionMessage) => void): MessageStream;
        addHandler(path: string, handler: MessageHandler): Result < void, any > ;
        addRequestHandler < T > (path: string, handler: RequestHandler, keys ? : Array < any > , requestType ? : DataType < T > ): Result < Registration, any > ;
        sendRequest < T, U, V > (path: string, request: any, sessionIdOrRequestType ? : Object | DataType < T > , requestTypeOrResponseType ? : DataType < U > , responseType ? : DataType < V > ): Result < Object, any > ;
        sendRequestToFilter < T, U > (filter: string, path: string, request: any, callback: FilteredResponseHandler, requestType ? : DataType < T > , responseType ? : DataType < U > ): Result < number, any >
            setRequestStream < T, U > (path: string, stream: RequestStream, requestType ? : DataType < T > , responseType ? : DataType < U > ): RequestStream;
        removeRequestStream(path: string): RequestStream;
    }
    export interface MessageStream extends Stream {}
    export interface Message {
        path: string;
        content: any;
    }
    export interface SessionMessage extends Message {
        session: string;
        options: any;
        properties ? : any;
    }
    export interface SendResult {
        path: string,
            recipient: string;
    }
    export interface RequestHandler {
        onRequest: (request: any, context: RequestContext, responder: Responder) => void;
        onError: (error ? : Error) => void;
        onClose: () => void;
    }
    export interface FilteredResponseHandler {
        onResponse: (sessionId: Object, response: any) => void;
        onResponseError: (sessionId: Object, error ? : Error) => void;
        onError: (error ? : Error) => void;
        onClose: () => void;
    }
    export interface Registration {
        close: () => Result < void, any > ;
    }
    export interface Responder {
        respond: (response: any) => void;
    }
    export interface RequestContext {
        SessionId: Object;
        path: string;
        properties: Object;
    }
    export interface RequestStream {
        onClose: () => void;
        onError: (error: Error) => void;
        onRequest: (path: string, request: any, responder: Responder) => void;
    }
    export interface Ping {
        pingServer: () => Result < PingDetails, any > ;
    }
    export interface PingDetails {
        timestamp: number;
        rtt: number;
    }
    export interface Security {
        authenticationScriptBuilder: () => SystemAuthenticationScriptBuilder;
        changePrincipal: (principal: string, credentials: string) => Result < void, any > ;
        getPrincipal: () => string;
        getSecurityConfiguration: () => Result < SecurityConfiguration, any > ;
        getSystemAuthenticationConfiguration: () => Result < SystemAuthenticationConfiguration, any > ;
        securityScriptBuilder: () => SecurityScriptBuilder;
        updateAuthenticationStore: (script: string) => Result < void, any > ;
        updateSecurityStore: (script: string) => Result < void, any > ;
    }
    export interface SystemPrincipal {
        name: string;
        roles: string[];
    }
    export interface Role {
        name: string;
        global: string[];
        default: string[];
        topic: Object;
        inherits: string[];
    }
    export interface SystemAuthenticationScriptBuilder {
        abstainAnonymousConnections: () => SystemAuthenticationScriptBuilder;
        addPrincipal: (principal: string, password: string, roles ? : string[]) => SystemAuthenticationScriptBuilder;
        allowAnonymousConnections: (roles ? : string[]) => SystemAuthenticationScriptBuilder;
        assignRoles: (principal: string, roles: string[]) => SystemAuthenticationScriptBuilder;
        build: () => string;
        denyAnonymousConnections: () => SystemAuthenticationScriptBuilder;
        verifyPassword: (principal: string, password: string) => SystemAuthenticationScriptBuilder;
    }
    export interface SecurityScriptBuilder {
        removeTopicPermissions: (role: string, path: string) => SecurityScriptBuilder;
        setDefaultTopicPermissions: (role: string, permissions ? : string[]) => SecurityScriptBuilder;
        setGlobalPermissions: (role: string, permissions: string[]) => SecurityScriptBuilder;
        setRoleIncludes: (role: string, roles ? : string[]) => SecurityScriptBuilder;
        setRolesForAnonymousSessions: (roles ? : string[]) => SecurityScriptBuilder;
        setRolesForNamedSessions: (roles ? : string[]) => SecurityScriptBuilder;
        setTopicPermissions: (role: string, path: string, permissions: string[]) => SecurityScriptBuilder;
        build: () => string;
    }
    export interface SecurityConfiguration {
        named: string[];
        anonymous: string[];
        roles: Role[];
    }
    export interface SystemAuthenticationConfiguration {
        principals: SystemPrincipal[];
        anonymous: {
            action: string,
            roles: string[]
        }
    }
    export interface EventMetadata {
        sequence: number;
        timestamp: number;
        author: string;
    }
    export interface Event {
        metadata: EventMetadata;
        originalEvent: EventMetadata;
        value: any;
        sequence: number;
        timestamp: number;
        author: string;
        isEditEvent: boolean;
        isOriginalEvent: boolean;
    }
    export interface QueryResult {
        events: Event[];
        selectedCount: number;
        isComplete: boolean;
        merge: (other: QueryResult) => QueryResult;
    }
    export interface RangeQuery {
        allEdits: () => RangeQuery;
        as: (valueClass: Function | DataType < any > ) => RangeQuery;
        editRange: () => RangeQuery;
        forEdits: () => RangeQuery;
        forValues: () => RangeQuery;
        fromSequence: (sequence: number | Date) => RangeQuery;
        fromLast: (sequence: number) => RangeQuery;
        fromLastMillis: (timeSpan: number) => RangeQuery;
        fromStart: () => RangeQuery;
        latestEdits: () => RangeQuery;
        limit: (count: number) => RangeQuery;
        next: (count: number) => RangeQuery;
        nextMills: (timeSpan: number) => RangeQuery;
        previous: (count: number) => RangeQuery;
        previousMills: (timeSpan: number) => RangeQuery;
        selectFrom: (path: string) => Result < QueryResult, ErrorReason > ;
        to: (sequence: number | Date) => RangeQuery;
        toStart: () => RangeQuery;
        untilLast: (count: number) => RangeQuery;
        untilLastMills: (timeSpan: number) => RangeQuery;
    }
    export interface TimeSeries {
        append: (topicPath: string, value, valueType ? ) => Result < EventMetadata, ErrorReason > ;
        edit: (topicPath: string, originalSequence, value, valueType ? ) => Result < EventMetadata, ErrorReason > ;
        rangeQuery: () => RangeQuery;
    }
    export interface TopicControl {
        add: (path: string, supplied ? : any, initial ? : any) => Result < AddResult, TopicAddFailReason | CloseReason | Error > ;
        remove: (path: string) => Result < RemoveResult, any > ;
        removeWithSession: (topicPath: string) => Result < RemoveWithSessionResult, any > ;
        update: (path: string, value: any) => Result < string, any > ;
        registerUpdateSource: (topicPath: string, handler: UpdateSourceHandler) => any;
        addMissingTopicHandler: (path: string, handler: MissingTopicHandler) => Result < any, any > ;
    }
    export interface Updater {
        update: (topicPath: string, value: any) => Result < void, any >
    }
    export interface AddResult {
        topic: string,
            added: boolean
    }
    export interface RemoveResult {}
    export interface RemoveWithSessionResult {
        deregister: () => Result < void, any > ;
    }
    export interface MissingTopicHandler {
        onClose: (topicPath: string) => void;
        onError: (topicPath: string, error: any) => void;
        onMissingTopic: (notification: MissingTopicNotification) => void;
        onRegister: (topicPath: string, deregister: any) => void;
    }
    export interface MissingTopicNotification {
        path: string;
        selector: TopicSelector;
        sessionID: string;
        cancel: () => void;
        proceed: () => void;
    }
    export interface Notifications {
        addListener(topicNotificationListener: TopicNotificationListener): Result < TopicNotificationRegistration, any > ;
        TopicNotificationType: {
            ADDED: TopicNotificationType;
            SELECTED: TopicNotificationType;
            REMOVED: TopicNotificationType;
            DESELECTED: TopicNotificationType;
        }
    }
    export interface TopicNotificationListener {
        onDescendantNotification(topicPath: string, type: TopicNotificationType): void;
        onTopicNotification(topicPath: string, topicSpecification: TopicSpecification, type: TopicNotificationType): void;
        onClose(): void;
        onError(error: any): void;
    }
    export interface TopicNotificationRegistration {
        select(topicSelector: string | TopicSelector): Result < void, any > ;
        deselect(topicSelector: string | TopicSelector): Result < void, any > ;
        close();
    }
    export interface TopicNotificationType {
        id: number;
    }
    export interface Topics {
        subscribe: (selector: string, callback ? ) => Subscription;
        unsubscribe: (selector: string) => void;
        stream: (selector: string, callback ? ) => Subscription;
        view: (selector: string, callback ? ) => View;
        fetch: (selector: string) => FetchStream;
    }
    export interface MessageHandler {
        onMessage: (message: SessionMessage) => void;
        onActive: (unregister: any) => void;
        onClose: () => void;
    }
    export interface UpdateSourceHandler {
        onRegister ? : (topicPath ? : string, unregister ? : () => void) => void;
        onActive ? : (topicPath ? : string, updater ? : Updater) => void;
        onStandBy ? : (topicPath ? : string) => void;
        onClose ? : (topicPath ? : string) => void;
    }
    export interface Metadata {
        String: IString;
        Integer: Integer;
        Decimal: Decimal;
        Stateless: Stateless;
        RecordContent: RecordContent;
    }
    export interface IString {
        new(str ? : string): IString;
        value: string;
    }
    export interface Integer {
        new(num ? : number): Integer;
        value: number;
    }
    export interface Decimal {
        new(num ? : number, scale ? : number): Decimal;
        value: number;
        scale: number;
    }
    export interface Stateless {
        new(): Stateless;
    }
    export interface RecordContent {
        new(): RecordContent;
        occurs: (min: number, max ? : number) => Occurs;
        addRecord: (name: string, occurs ? : any, fields ? : any) => Record;
        getRecord: (key: number) => Record;
        getRecords: () => Record[];
        string: (str ? : string) => String;
        integer: (num ? : number) => Integer;
        decimal: (num ? : number, scale ? : number) => Decimal;
        builder: () => RecordContentBuilder;
        parse: (buffer: any) => RecordContent;
    }
    export interface RecordContentBuilder {
        add: (name: string, fields ? : any) => Record;
        set: (name: string, fields ? : any, index ? : number) => Record;
        build: () => RecordContent;
        addAndBuild: (name: string, fields ? : any) => RecordContent;
        setAndBuild: (name: string, fields ? : any, index ? : number) => RecordContent;
    }
    export interface Record {
        name: string;
        occurs: Occurs;
        addField: (name: string, type: any, occurs ? : any) => Field;
        getField: (key: number) => Field;
        getFields: () => Field[];
    }
    export interface Field {
        name: string;
        type: String | Integer | Decimal;
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
    export interface TopicAddFailReason {
        id: number;
        reason: string;
    }
    export interface TopicSpecification {
        type: TopicType;
        properties: Properties;
    }
    interface Properties {
        [n: string]: string;
    }
}