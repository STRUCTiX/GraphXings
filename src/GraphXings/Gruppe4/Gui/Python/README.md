## Requirements

### Ubuntu
```
 sudo apt install -y python3-venv
 deactivate
 rm venv
 python3 -m venv venv
 source venv/bin/activate
 # unalias pip
 pip install networkx matplotlib agg
```

## Run

Simple:

```
python Python/graph.py $(ls *.txt | head -n 1)
```

Run in a loop:

``` 
for f in *.txt ; do python Python/graph.py ${f} ; done
```