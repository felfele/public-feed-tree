# public-feed-tree

## Workflow

### Usenet

We use usenet.txt to generate initial categories. The output is _tumblrssUsenetMap.json_. Later it's copied to resources (atm also renamed to _usenetMap.json_) This is where we can modify the categories.

### ContentScraperSimple

Input _usenetMap.json_ output _usenetTumblrTree.json_.

### UsenetFixDepth

Modifies the _usenetTumblrTree.json_ to a fixed depth tree. The output is _threeLevelTree.json_.

### UsenetAddReddit

UsenetAddRreddit: threeLevelTree.json - > usenetTumblrReddit.json
Consumes the original and the fixed depth json and adds reddit newssources. The output is _usenetTumblrReddit.json_.





