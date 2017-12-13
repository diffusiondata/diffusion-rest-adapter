declare module "diffusion" {
    export var version: string;
    export function connect(options: Options): Result < Session, ErrorReason > ;
    export function log(level: string): void;
    export var datatypes: DataTypes;
    export var selectors: TopicSelectors;
    export var metadata: Metadata;
    export var topics: {
        TopicType: {
            JSON: TopicType;
            BINARY: TopicType;
            RECORD: TopicType;
            SINGLE_VALUE: TopicType;
        };
        UnsubscribeReason: {
            REQUESTED: any;
            CONTROL: any;
            REMOVED: any;
            AUTHORIZATION: any;
        };
    };
    export var errorReport: ErrorReport;
    export var clients: {
        PropertyKeys: {
            ALL_FIXED_PROPERTIES: any;
            ALL_USER_PROPERTIES: any;
        };
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
    export interface Options {
        host ? : string;
        port ? : number;
        path ? : string;
        secure ? : boolean;
        principal ? : string;
        credentials ? : string;
        reconnect ? : boolean | number | Function | ReconnectOptions;
        transports ? : string | string[];
    }
    export interface ReconnectOptions {
        timeout: number;
        strategy: Function;
    }
    export interface Session extends Topics, Stream {
        close: () => Session;
        isConnected: () => boolean;
        isClosed: () => boolean;
        security: Security;
        topics: TopicControl;
        messages: Messages;
        clients: ClientControl;
        options: Options;
        sessionID: string;
        toString: () => string;
        stream: (selector ? : string, callback ? : Function) => Subscription;
    }
    export interface TopicType {
        id: number;
        stateful: boolean;
        functional: boolean;
    }
    export interface BinaryDataType extends DataType < Binary > {}
    export interface Binary {
        apply: (delta: any) => Binary;
        diff: (original: any, type: string) => BinaryDelta;
        get: () => any;
    }
    export interface BinaryDelta {
        hasChanges: () => boolean;
    }
    export interface DataType < T > {
        from: (buffer) => T;
        name: string;
        readValue: (input: any, offset ? : number, length ? : number) => any;
        writeValue: (value) => any;
        deltaType: (name: string, delta) => any;
    }
    export interface DataTypes {
        binary: () => BinaryDataType;
        json: () => JsonDataType;
        double: () => any;
        int64: () => any;
        get: (name: any) => DataType < Json > | DataType < Binary > ;
        BinaryDataType: BinaryDataType;
        JSONDataType: JsonDataType;
        Binary: Binary;
        JSON: Json;
        BinaryDelta: BinaryDelta;
        JSONDelta: JsonDelta;
    }
    export interface JsonDataType extends DataType < Json > {
        fromJsonString: (str: string) => Json;
    }
    export interface Json {
        apply: (delta: any) => Json;
        diff: (original: any, type: string) => JsonDelta;
        get: () => any;
    }
    export interface JsonDelta {
        hasChanges: () => boolean;
    }
    export interface FetchStream extends Stream {}
    export interface Result < T, E > {
        then < TResult,
        TE > (fulfilled: (value: T) => TResult | Result < TResult, TE > , rejected ? : (reason: any) => void): Result < TResult,
        E > ;
    }
    export interface SessionPropertiesResult < T, E > {
        then < TResult,
        TE > (fulfilled: (session: string, properties: string[]) => TResult | SessionPropertiesResult < TResult, TE > , rejected ? : (reason: any) => void): SessionPropertiesResult < TResult,
        E > ;
    }
    export interface Stream {
        on: (events: string | Object, listener ? : Function) => Stream;
        off: (event: string, listener: Function) => Stream;
        close: () => void;
    }
    export interface Subscription extends Stream {
        selector: string;
        asType: (type: DataType < Binary > | DataType < Json > ) => TypedSubscription;
        view: View;
        transform: (transformer) => Subscription;
        close: () => void;
    }
    export interface TypedSubscription extends Subscription {}
    export interface View extends Stream {
        get: () => any;
    }
    export interface ClientControl {
        subscribe: (session: string, selector: string) => Result < number, any > ;
        unsubscribe: (session: string, selector: string) => Result < number, any > ;
        getSessionProperties: (sessionID: string, requiredProperties: string[]) => SessionPropertiesResult < any, any > ;
        setSessionPropertiesListener: (requiredProperties: string[], listener: SessionPropertiesListener) => Result < void, any > ;
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
        send: (path: string, message: any, sessionID ? : string) => Result < SendResult, any > ;
        listen: (path: string, listener ? : (message: SessionMessage) => void) => MessageStream;
        addHandler: (path: string, handler: MessageHandler) => Result < void, any > ;
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
    export interface TopicControl {
        add: (path: string, supplied ? : any, initial ? : any) => Result < AddResult, any > ;
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
    export interface Topics {
        subscribe: (selector: string, callback ? ) => Subscription;
        unsubscribe: (selector: string) => void;
        stream: (selector: string, callback ? ) => Subscription;
        view: (selector: string, callback ? ) => View;
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
        new(str ? : string): String;
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
}
