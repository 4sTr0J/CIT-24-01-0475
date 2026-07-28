## Task 1.2 - Control-Plane Components Identification

| **Component** | `Pod Name(s) in kube-system` | Purpose |
|-----------|---------------------------|---------|
| **API Server** | `kube-apiserver-minikube` | Exposes Kubernetes API; frontend for control plane |
| **etcd** | `etcd-minikube` | Distributed key-value store; stores cluster state |
| **Scheduler** | `kube-scheduler-minikube` | Assigns pods to nodes based on resource requirements |
| **Controller Manager** | `kube-controller-manager-minikube` | Runs controller processes (Deployment, Node, etc.) |
| **Cloud Controller Manager** | `cloud-controller-manager-minikube` | (May not appear) Manages cloud provider integrations |
| **kube-proxy** | `kube-proxy-xxxxx` | Network proxy; maintains network rules on nodes |


### Components That Do NOT Appear as Pods:

| **Component** | Why It Doesn't Appear as a Pod |
|-----------|--------------------------------|
| **Container Runtime** (Docker/containerd) | Runs as a system daemon on each node, not as a Kubernetes pod |
| **kubelet** | Runs as a system service on each node; primary node agent |
| **kube-proxy** (in some setups) | Can run as a DaemonSet pod or as a system service depending on configuration |



Checkpoint Q1. In your own words, explain the difference between the control plane and a worker node.

        The control plane is the centralized management layer of Kubernetes cluster, while the worker-nodes are the machine that actually run the applications and the workloads.


Checkpoint Q2. Delete the pod (`kubectl delete pod frontend`), then recreate it from the same manifest and check its IP with `kubectl get pods -o wide`. Has the IP changed? Explain why using the lecture's description of Pods as "ephemeral."

            Yes, the IP changes. This is because the Pods in Kubernetes are ephermeral(likely volatile). When the pod is deleted, it will remove all the traces of that corresdponding pod. Thereafter, the newly created pod will get a fresh IP.


Checkpoint Q3. Using the lecture's control-loop model — Desired State → Controller watches → Actual State → Gap Detected → Reconcile — describe, step by step, exactly what Kubernetes did when you deleted the pod

        1. The Desired State, tracks the pods running.
        2. Then, Controller Watches: Continuously monitors the cluster through the API server, checking what pods exist and whether they match the desired state.
        3. Actual State, indicates that the pod is deleted and no pod is currently running.
        4. Gap Detected, finds that there is a mismatch between the desired state and actual state.
        5. Finally, Reconile helps the system to close the gap and reschedule a new pod on a worker node, pull a specified image such as nginx and to start it.


Checkpoint Q4. The lecture's "Applications Are Multiple Containers" slide states that each service can scale independently. Once you deploy the database tier in Part 7, why will you be able to scale the frontend without touching it?
        
        After you deploy the database tier, Kubernetes treats the front-end and database tier as different services that have their own pods and configuration. The frontend does not have to know the individual IP addresses of the pods that are running the database; instead it links through the database service. The separation allows you to scale the frontend independently: you can add additional frontend pods to accommodate increased traffic to the application without changing or redeploying the database tier. It shows that applications are constructed using various app-services that are running in containers where each individual services has the capacity to scale independently without affecting the different services.


Checkpoint Q5. What is the difference between accessing a Pod directly via port-forward (Part 2) and accessing it through a Service (Part 5)? Why do Services matter, given that Pods are ephemeral and get new IPs when replaced?

        Port-forward is temporary and only for one specific pod. Services provide stable access points that load-balance across pods and maintain connectivity even when pods are replaced with new IPs.


Checkpoint Q6. Referring to the lecture's list of things "Docker Compose Cannot" do, explain why this same update-and-rollback would be much harder to do safely with Docker Compose alone.

        Using Docker Compose, the new versions of the containers are replaced seamlessly, but they lack health checks, gradual rollouts, and automatic rollback features. Otherwise, it will need to be manually corrected if the new version is unsuccessful. There's more safety to update a pod and roll back; Kubernetes monitors the health of the pod, and it can roll back safely. Compose makes update‑and‑rollback very hard to roll back safely.


Checkpoint Q7. Explain why the frontend and API tiers use a Deployment while the database tier uses a StatefulSet. Refer to the lecture's Stateless vs Stateful comparison (pod naming, storage, ordering).

        StatefulSets create stable and unique network identifiers (such as "postgres-0"), sequential deployment and scaling, and persistent storage that doesn't progress lost throughout recreation of pods. Deployments do not provide guaranteed identities of storage to be used with pods and consider the pod as interchangeable.

    
Checkpoint Q8. Would this data have survived if postgres had instead been deployed as a plain Deployment 
without a PersistentVolumeClaim? Explain your reasoning.

        No. Without a PersistentVolumeClaim, data would be lost when the pod is deleted because it would be stored in the ephemeral container filesystem. PVC ensures data persists on storage that outlives the pod.


Checkpoint Q9. What status did the broken pod show? Compare it against the lecture's Pod Status table (Running / Pending / CrashLoopBackOff / OOMKilled) — does it match one of these exactly, or is it a related status not explicitly listed? Explain what it means.

        If a pod has an `ErrImagePull` or a `ImagePullBackOff` condition, Kubernetes has got into the Pending state because it cannot pull the container images. Typically caused by incorrect image name/tag, if the image fails to exist, or if the cluster can't reach/authenticate with registry. Kubernetes will continue to retry with backoff until it resolves that issue and you can't access the registry or edit the manifest until that is addressed.

        