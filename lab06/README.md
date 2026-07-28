CIT-24-01-0475 | NADIL KULARATHNE

CCS3308 - Virtualization and Containers
Week 7 - Container Orchestration & Kubernetes
Lab 06

This lab demonstrates Kubernetes fundamentals using Minikube, a local single-node Kubernetes cluster. The project deploys a complete multi-tier application consisting of:

- **Frontend Tier**: Nginx web server serving static content
- **API Tier**: HTTPBin REST API service
- **Cache Tier**: Redis in-memory data store
- **Database Tier**: PostgreSQL with persistent storage

The lab showcases key Kubernetes concepts including Pods, Deployments, Services, StatefulSets, PersistentVolumeClaims, self-healing, scaling, rolling updates, and rollbacks.


┌─────────────────────────────────────────────────────────────┐
│                     Kubernetes Cluster                      │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Frontend Tier (nginx:alpine)            │   │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐               │   │
│  │  │ Pod 1   │  │ Pod 2   │  │ Pod 3   │               │   │
│  │  └─────────┘  └─────────┘  └─────────┘               │   │
│  │         │                                            │   │
│  │    NodePort Service (Port 30001)                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              API Tier (kennethreitz/http-bin)        │   │
│  │  ┌─────────┐  ┌─────────┐                            │   │
│  │  │ Pod 1   │  │ Pod 2   │                            │   │
│  │  └─────────┘  └─────────┘                            │   │
│  │         │                                            │   │
│  │    ClusterIP Service (Port 80)                       │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           Cache Tier (redis:7-alpine)                │   │
│  │  ┌─────────┐                                         │   │
│  │  │ Pod 1   │                                         │   │
│  │  └─────────┘                                         │   │
│  │         │                                            │   │
│  │    ClusterIP Service (Port 6379)                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                          │                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │          Database Tier (postgres:16-alpine)          │   │
│  │  ┌─────────────────┐                                 │   │
│  │  │ StatefulSet     │                                 │   │
│  │  │ postgres-0      │                                 │   │
│  │  │ ──────────────  │                                 │   │
│  │  │ PVC: 1Gi        │                                 │   │
│  │  └─────────────────┘                                 │   │
│  │         │                                            │   │
│  │    Headless Service (Port 5432)                      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘