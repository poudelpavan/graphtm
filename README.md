## TraFex: Distributed Transactional Memory for Arbitrary Graphs

**Options:**
```
-g	 graph type, default grid [line, clique, grid, cluster, star]
-n	 total number of nodes, default 100
-k	 total subgrids in grid graph, default 1 (optional for line, clique, cluster, star)
-c	 total number of clusters, default 10 (optional for line, clique, grid, star)
-d	 size of each cluster in cluster graph, default 10 (optional for line, clique, grid, star)
-r	 total rays in star graph, default 14 (optional for line, clique, grid, cluster)
-s	 size of each ray in star graph, default 7 (optional for line, clique, grid, cluster)
-b	 benchmark type, default bank 
 	 [bank, ll, hs, rb, sl, bayes, genome, intruder, kmeans, labyrinth, ssca2, vacation, yada]
-t	 total number of threads in benchmark, default 8
```

**Example:**
```
GRID 	-->	-ggrid -n100 -k2 -bbank -t8
CLUSTER -->	-gcluster -c10 -d10 -bhs -t8
STAR    -->	-gstar -r14 -s7 -bgenome -t8
```
