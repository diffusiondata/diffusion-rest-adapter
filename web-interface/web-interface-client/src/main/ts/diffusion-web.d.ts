import {Session} from './session.d';
import {ErrorReason} from './error.d';
import {SessionOptions} from './options.d';
import {Result} from './events/result.d';
import {DataTypes} from './data/datatypes.d'
import {Metadata} from './metadata/metadata.d';
import {TopicSelectors} from './selectors/topic-selectors.d';
import {ErrorReport} from './error-report.d';
import {TopicTypeEnum} from './enum/topic-type-enum';
import {CloseReasonEnum} from './enum/close-reason-enum';
import {TopicAddFailReasonEnum} from './enum/topic-add-fail-reason-enum';
import {UnsubscribeReasonEnum} from './enum/unsubscribe-reason-enum';
import {PropertyKeysEnum} from "./enum/property-keys-enum";

type LogVerbosityLevel = "silent" | "error" | "warn" | "info" | "debug";

export var version: string;

export function connect(options: SessionOptions): Result<Session, ErrorReason>;

export function log(level: LogVerbosityLevel): void;

export var datatypes: DataTypes;
export var selectors: TopicSelectors;
export var metadata: Metadata;
export var topics: {
    TopicType: TopicTypeEnum;
    UnsubscribeReason: UnsubscribeReasonEnum;
    TopicAddFailReason: TopicAddFailReasonEnum;
};
export var errorReport: ErrorReport;
export var clients: {
    PropertyKeys: PropertyKeysEnum,
    CloseReason: CloseReasonEnum
};
