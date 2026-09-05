/**
 * ============================================================================
 * Spring Boot Actuator Telemetry Models (`actuator.models.ts`)
 * ============================================================================
 * DTO contracts representing health probes and runtime diagnostics exposed
 * by Spring Boot Actuator endpoints (`/actuator/health` and `/actuator/info`).
 */

export interface DbHealthDetails {
  database?: string;
  validationQuery?: string;
  [key: string]: any;
}

export interface DiskSpaceDetails {
  total?: number;
  free?: number;
  threshold?: number;
  [key: string]: any;
}

export interface HealthComponent<T = Record<string, any>> {
  status: string;
  details?: T;
}

export interface ActuatorHealth {
  status: 'UP' | 'DOWN' | 'OUT_OF_SERVICE' | 'UNKNOWN';
  components?: {
    db?: HealthComponent<DbHealthDetails>;
    diskSpace?: HealthComponent<DiskSpaceDetails>;
    ping?: HealthComponent;
    [key: string]: HealthComponent<any> | undefined;
  };
}

export interface ActuatorInfo {
  app?: {
    name?: string;
    description?: string;
    version?: string;
    java?: {
      version?: string;
      vendor?: string;
    };
  };
  [key: string]: any;
}
