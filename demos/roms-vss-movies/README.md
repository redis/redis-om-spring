# Vector Similarity Search Demo

This demo showcases Redis 8's vector similarity search capabilities using Redis 8 & [Redis OM Spring](https://github.com/redis/redis-om-spring). It implements a movie recommendation system that demonstrates vector similarity search features provided by the Redis Query Engine.

## Features
### Vector Similarity Search

Vector similarity search (provided by the Redis Query Engine) enables semantic search and similarity matching using vector embeddings. This is particularly useful for:
- Semantic search across movie descriptions
- Finding similar movies based on content
- Content-based recommendations
- Multi-modal search (text, images, etc.)

### Semantic Search

The demo implements a semantic search system that can:
- Find movies based on semantic meaning rather than exact matches
- Provide relevant results even when exact terms don't match
- Support hybrid search combining text and vector queries

## Getting Started

1. **Start the Redis instance**:
   ```bash
   docker-compose up -d redis-vector-search
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

The application exposes the following REST endpoints:

### GET /search

**Purpose:** Search for movies using vector similarity search on the synopsis or hybrid search on year, genres, or cast

**Example:** /search?numberOfNearestNeighbors=1&yearMin=1970&yearMax=1990&text=A movie about a kid and a scientist who go back in time


**Description:**
Returns a list of movies whose synopsis (extract) are semantically similar to the search query, even if the exact terms don't match.
The first element of the response is the relevant movie and the second is the similarity score.

**Response example:**
```
{
  "count": 1,
  "matchedMovies": [
    {
      "first": {
        "title": "Back to the Future",
        "year": 1985,
        "cast": [
          "Michael J. Fox",
          "Christopher Lloyd",
          ...
        ],
        "genres": [
          "Science Fiction"
        ],
        "href": "Back_to_the_Future",
        "extract": "Back to the Future is a 1985 American science fiction film directed by Robert Zemeckis and written by Zemeckis, and Bob Gale. It stars Michael J. Fox, Christopher Lloyd, Lea Thompson, Crispin Glover, and Thomas F. Wilson. Set in 1985, it follows Marty McFly (Fox), a teenager accidentally sent back to 1955 in a time-traveling DeLorean automobile built by his eccentric scientist friend Emmett \"Doc\" Brown (Lloyd), where he inadvertently prevents his future parents from falling in love – threatening his own existence – and is forced to reconcile them and somehow get back to the future.",
        "embeddedExtract": "EqY8vBhaubsaOI6801kHPIaS17wTTti8ogrmO53lkzwIHaW8RMAxPQvyPz1GcpQ7DfPMvNf59TyHF7u86cU0uYwjnL3ANFc9SfgEPRWxEL1oGTy6OrI2vOUcjL3sHzM9ahrJvCh0lTwR0q67mPxqPCeghzwOm2i8EvVmO06YczyoIA48TFKDveW6YLv/MeI7Z3GgvESKeDvQ05Y8ipRxu8fymjxR8vE8mzOxOX0seLzKx7W7oZgDPbcwo7yark29PGSZu36FabtBZjO9RUWVvN9oj7wYC4885O8MvM5E7Lz0sQQ8SJ+TPBSnybz9f/+8EU3Lu/R7y7zAahC94hE4PEzwVz1tG9Y8ZGZMPcceDT2syTY9KhN3vFRM8DxbNia8uF2iPLd/zbxoGbw89VmgOm8c47uFbx88+gwQvX2OI70Z3xw78nq+uz+I3ryR1gs92FLnu/oMkLxyUyk9WlhRvU0mETyiuzs98P4UPdTVsDzYL6+8CfEyvf/it7x0BYy8z3olPVuxwjz+Diq93q3yvL89kbqoIA69HgTvO9sOkbw+4MI7+ls6vZPXGL2Mnrg8cx59PHnbMzw0IXK8dNkZvV+z3LwluGs8p8ecvFVWtzwhwJg7hOD0PCzFWT3PeqW8rE4aPdDTFr3tpBa99LEEPJ6Nr7slacE76RTfvHF/mzyUf7Q8baA5Ow6b6LwhXu08Wf9fvM71QT36fnK89i2uOniCQruB6CG9qk0NPduJrTyobzg8mK3APBnfnDzDjtW8dFS2PDDQLb2HyBA850kLPQtB6joWWaw79nxYvFeDNr2JToE8w91/uqfzDj0qxEw7rXFSvNgDPTyI9Y+8hW+fuVrdNLwB5MS8FPbzO0VFFbvteCS71FDNu17oCD2jFC09bvkqvKK7uzxBF4k7L/wfvDV647qUomy7ml+jvI1G1DvhB/E8yUsMvIr2nLyC3+e8vmkDvA4phjzL6u08jZX+O4t7ADy4J2m6F1DyvDbTVDynxxw9BhwYvRwDYjysGGE82atYPFJLY7x+Nr88nrDnPNjghLxNdbs8h+vIvGfASjzbiS29h+vIOYWbkTwRgwS8ODYNPQTvmDzWhxM8YOmVPPJ6Pjww0K28E07YPJxgsLx8hFw8hW8fOhFNSzvO9cG5WbA1vWgZPDzfaI+76rz6PAHBDD15jAm8+C47PG0bVrxLSLy8Lnc8Pf21uDuark09sqmlvGtQAr0pzQa8RMAxPJeKCL33hp88J2rOurjYPj0BM+86DfPMu3Amqrx10N88XlrrPP1mDjxbscK8+i/IvClr2zzluuA8QesWvfhaLboU3YK8erkIvD9lJj2RSG48+YcsPWMNWzynFkc8vYJ0PLMlzznK8ye7Y0OUvHvcQDzrFWy8+YcsPN6UgbvteKQ8fITcvJfZMrwB5ES8wLm6vATvGD3OROy8IpQmPGrLnr38OQ+7HVzTPKwY4TtxzkW8togHvRHSrrzbiS09BI3tO/9nGz2BvC89S0i8vKRtHr1Ozqw8NNJHPOzQCDxJc6G8b36OPIVvn7uoTIC8SqAgu4VvHzwM/AY8JRqXO6PFArvfBuQ8vritu28cY7yQUag8I196PTY1ALsOm2i86RTfvMfymrxpci085Zeou6S8SD2l8oG8eSpevXD6tzwxx3O8RxowvXAmKjwJQF09S0g8PACLU7wgttG8x5DvvP8xYrvRnuo7urYTu5P6UDuXigi9IxDQPCMQ0LwKHjK8WNwnvFlhi7wMd6M8eoPPvAEQtzz4feW8LW11u0K/JDqHnB69GVF/vWrLHj0HxLO6jp/FvNMtlbyoTAA8PTgnvKibKjzPeiW9bsNxvYc6czu3f028+i9IPP2SALx7K+s5KRyxvOO507xOScm75BLFPP+TjbsOTL488nq+vMGNSDtPJx49yR8aPEGJazs3WDg8b804vGZnWb20Wwi96xXsvL89kbwzeda8uF0iO8Y3fjzerXI8VK6bvKtwRTz0sYQ8wdzyO1CAjzwP0aE8nVd2PINukjy/22W8kFGoue5vaj0B5MQ8HZIMvZitwLwwVRG9mVVcvDKCEL1y2Ay8Xlpru9BF+byvct+84K5/PHQoRLzc4p68j0dhPIpFRz3yyWg8FFifupb7XT1ncSA7Vq8oPZ5hPb1WTf28zHkYPPGmsLz8OY+8VYIpPWbJBD1gDE48gIb2O24lnbvhB3G95BLFO7CoGD2UU8K7IV7tPMC5OjvzWBM7y5tDvA4pBjvll6g8sBp7O9aHEz2uexk9aGhmPNmIoDyduaG7rE6avB+/Cz3oHRm9jlAbvJr9dzwxeMm8p/OOvDAfWD3rS6W8YUKHO4OahDxW25o8K2xouBfeD72Xioi8qpw3vXPPUj0N88y7k/rQu5HWi7h85oe9HgRvu3B1VDyF6ru8DpvougPl0bsuxmY82FJnu1+zXDu9M8o6k6umPHsr6zygCdk85GHvPO14pLxOfwK6vNrYvHOAqLykbZ47y5vDvGGRsTo6Yww9B8QzPcjGKD0SK6A7k/pQPdhS5zzrSyU8qhdUuxipYzwwH9i7YFv4PMdBxTwR0i49jE8OvaXGjzwdXFO7oWLKvPbeg7yoTIC8kUjuOVdXxDzO9UG9utnLuzpjDLz9f/+6Kx2+vHw1MrxxHfA5sKgYPfovSLvpxbQ7rcB8vHgzGL1bYpg87tEVvZBRqDy1AyQ8pWTku5P6UL1KoCC8eNHsvGtzOj0OKYY71S4ivMrzJ7yOn0U7HAPivPk4gjxOScm7L/yfvCsdPjzXqsu7EZx1PEn4BD1o9oO8g+muuxnfHD1nwMo776WjvOt3l7zT91s8bnTHPFtimLpcWd68t39NPSbCsrvFj+I6tzAjPU+iOrvcBVe9rcB8vEGJ6zuYMiS90PbOOw1C97zDjtW8asuePLEkwrz/kw28xfGNvfguOzvtx048V1fEOmBb+DwyIOU8fOaHO1qOirusGOE7GIYrPGpp87v1I2c8CplOO5OrprzJmjY8wz8rvYfrSLwOTL68x0FFvNmr2LtrwuQ8/eGquhao1rv7ZQE9FlksveJgYruY/Oo8RDvOPHUyizsqE/e7eAcmu4THg7yv1Ao88HB3OtP327pSS+M8A8KZPMsgp7y8t6C8ANr9O/QsIT1MzZ+8mF6WvCNferzN63q8O9Xuu+kUX7wsoqG7JkcWPIj1jzw4No27zPQ0O+Rh7zwuxmY8wjVkvJvkBr2YXpa8IpQmPAfwpbzTqLG7wJYCOpFI7rw91ns7O4bEvKS8yDySfqc7C6MVvGW/PbwpzQa7Z3Egu+3Hzjvou228tibcPMI1ZD0YNwE9RZS/vD29ijzTWQe75vAZvBT287zBjUi81IYGPTpjDD2uysO8pm6rvPbeAzzoHRk9pxZHvOryM7ykbZ48uIkUPLuB5zuBvK+7i3sAvLnihT2lxg+8r9QKPTUIAT0yIGW8LCcFOwtBaj2p9Ju7ARC3O4lOAbynZXE87yqHPBOEETyxJMI7vNpYu9WpPrzJSwy8M/45vDyQCz2hsfQ82Q2EvPFXhrywGvu8gBQUPR5mGroUp8k89t6DPGtQgjq6tpM84mDiu3TZGbqvTye8xOdGPJgyJL3brOU8JnMIvEmW2Tt/QAa82LSSvF+QJL3ub+o7TM2fvP0wVbxO+h47l9myvFWlYTx93U0945YbvESK+LuGQ607mwe/udmrWLyG9IK7NIOdPAM0/LvOITS7FqjWPBkCVTtrnyy5E7CDvBJXkjpUTPC8J6AHvZGqmbwX3g+9Y0OUuyZHFr2nx5y72uERPZquTTsSKyC90Z5qPF+zXDzANNc6a58sPD4v7bnZDQQ9DikGPC53vDyC3+e7KBJqu6XyAbygawQ8nQjMO8I15Dt1gTW8iPUPvNaHk7t0BYy8d1+KPAroeLgOTD483Yq6PKshG7ulQSy8+TiCvBTdAj3ZDYQ8uzK9PMkfGrynxxy7CXaWvFMGgLzjudO8YL2jPCsdvrwZAlU82107PXuNljs+kZg82avYvMTnRrpRKCu78P6UPNkNBL3oHRk9Fy06vKPoOjxajoo8+uAdPGcPdbwOm2i8XuiIvKrIqbzFHYC8QTpBvFCsAbzrdxe8HQ0pu2W/vbuga4S8pWTkuyVpwTsnoAc9a8JkvJj8ajto9oO8ZQ7ovINuEjwWCoK8H11gvFBKVrw2NYC8cx79t/p+cjz0LCE8PN+1PGFCB7wP9Nk8AozgvD2HUToYqeO8cc7Fupyv2ryWrDO8swIXvAPl0TxBFwm7Lnc8OgRqtbyPR2G9nIyiPLjYPjycEQY9qEwAvQM0/Dq62Us7cc5FvKUVujxJR688U1UqOwvyPzy2iIc86m1QvKYfgTpK78o7x0FFvNgDPT2BvC88nVf2uzotUzt4gkI8cPo3PZIDizpTBgC9YFv4PHJTKT0gttG85MOaO3EdcLwodJW88yJauyrEzDw9OKe6EiugvLIuibzMQ1+83AXXutgDvbp0BQw89yR0PE+iursRTcs7k9cYPHD6t7gZs6o8J2pOu8kfmjwvKBK9HAPiPBKmvLy3zne8F1DyOyW4a7yxAQo9utlLOvzX4zvB3HK8KMM/u5lV3Dz64J08QxiWunzmBz16uQi7g+kuPAKMYDxRVB282YigvEUZIzzNnFC8f48wPHJ24byUf7S8kKDSvEXj6bwU9nM8zZzQuyh0FTykvMg6fhOHPEg96LxoypG8ryO1PErvSr3tx0671lFau+TvDLzhuMa8eAemPMHccrwgZ6e8J+8xPDJWnjy9gnQ8wjVkvSW467puqoA8R5VMPOi7bTu5gFo81lFau9D2Tryga4Q8aPYDPQujFTw/ZSY7fjY/PG75KjxC4ty7qfSbux8ONrwMmts8cMR+PCUaFzuExwO92LSSPLrZSzzu/Ye8f95aPAmiiDxjvjA8DdCUPD6RGL2RSG68H7+LvPQALzyDmoS8ZmfZu6GxdDwN88y6xBO5vN5eSDvRe7K86XaKu6NjV7t0VDY7jfepuwmiCDyh5628racLvWDpFbx73EA7aBk8vcGNSDyark08JxskPY5QG73zIto7JRqXOXQFDL28PAQ855g1u05JST0Mmlu7YL0jPEEXibx8hFy897IRPFKtDjx1rSe5C0FquvbegzrDPys6UaNHu+7RFbyMTw48RjzbvLoo9rwZUX+999VJPNZR2jynZfG6AozgPLaIh7nANFc7BkiKPNOosTzLm8M8EZx1PDdYuLpOfwI8wDRXPPJ6Pr1NdTu9UkvjPLEkQrzENvG7X2QyvZm3hzxW/tK7Sj71vBo4DjyCQZO8Rp6GvNGearxgDM67U1Uqu/mznrzAubo8avcQvdhS57z9f/+7tFuIPJBRqLoI5+u6Kx2+vGC9Iz2KcTm7bvmqvHo0JbzobMO7GKnjukE6wTyMnji9GIarO/mHrLtVgik9wpcPPOBf1bohD0M88nq+vAiYwbujY9c85GHvu83rejwP0aE7E/8tPP9nm7yJnau6HZKMO6K7Oz2/ER88gt9nvKdlcbzYA708CJhBvA2kIjxSS+M8NtNUvGtQAr3QRXm8N1i4PAtB6rydCMw8vLcgvFOk1Dz4CwM84sKNuqUVuryeja87rSIovI2V/jtOSck8n+YgvbFQNLwMmls8ZmdZPPnW1jxzgCg8YWU/vNBFeTwmRxa7DfNMvLB8Jj3F8Q08zvVBvMp4i7cit148+uAdvfharTq2Jly8VEzwvNMtlTywqBg70Xuyun+PMLze4ys6pr1Vu1RM8Lw4No28sQEKPITgdLwwgYM8hTnmuuOWmzw33Zs7NwmOu2ZnWbyyLok6oxStPP21OLy1L5Y88NKiO9kNBD3UhgY9TknJPFpY0btpI4O8/zFivLsPhTzLm8O81FBNPW7DcbuCkL26w47VOiFebTwVALs7g5oEvRkC1TzuTLI6AozgvBfejztxzkW7HGUNvVavqLw8LuA7HjoovH/e2jw1emO8nQhMvJFI7ru3BLE80SwIPF+z3LuuysM8ors7vH0seDsetcS7mzMxOi5UBD1GPNu7vxEfvC7GZjxfFQi8ILZROafzjrvV+Gg8GQJVvG19gbwHdYk7kaoZPGpp8zw41GE8fOaHvHPP0jscA+I7GFq5PHJTqTwdXFM8V6buuf8x4jpnwMq8DPyGO4CGdjyMIxw7j/i2vOMIfrynZfG7HrXEvMnpYLzJSwy8GVF/PJ0IzLy62Us6ILbRu9NZhzzQ0xa9yvMnvJlV3Lvw0qI7wY1IPI4kKbzD3f+8AZUaPYPprjzpdoq7rHqMuvR7y7pcWd65aPYDvE5/ArwYqWM8NiJ/PLHVF7wBM288b824PHTZmTtvfo48ZQ7oPJOrJjyWMZe7c8/Su1c0jDtzrBo8XbJPvH/e2jq9EJK8BZe0vD2HUTyom6q8SJ8TvN2KuruJTgE8qyEbPJR/tLurIZu8xuhTO9pTdLyZtwe8CJjBvACL07x3KdG7hzrzOxlkAD0T/y29ZLV2O2YYrzzHHg08ZXATuQPCmTzAlgI797KRPF4LwTvjuVM8RDtOPKRtHj1qGkm7DJpbvFdXxDvbDpE8kqHfu+MIfjpBZjO8xsUbPPXUPDwRTUu8geihOz85tDyKIg+81AGjPDcsxrwWWSw8cHVUPEK/pDw71e46ermIPA/02bwD5dE7HLQ3vKoX1Ly3MCM784SFPJGqmTwyIGU7ejSlO2C9o7zo8SY8rspDO0g96DwEG4s8ATNvvMceDT25MTA9iHAsuxUAuzvVWhQ83LYsPPMi2rwRTcs8rE4avB3htjxSS+O7RUWVPHfapjzN6/o7x/KavCdqTrw/6gk8PGSZPNCnpLzD8AA9Dkw+O7+MO7v/Z5u85brgvN1ngrwVsRC8wJYCO9WpPrwE75g8D6UvOhy0NzzE58a8dTKLvOi77Tkuo648p/OOPJwRhjseOqi876WjvFMGgLir9ai8S5fmPKYfATwuoy487iDAu657GT091ns8TPDXvHcpUTxtzCu86PGmPDDQrbyp9Bu9HAPivMI15DwYhis8qEwAPLN0eby42D47ZOuvua32NbxLl+Y7QkSIvAZICjyIIQK7pLzIvNsOkbwBM++5fd3Nug3zzDwnufg7acHXPP+TDT1bAO08D9GhvEDhTzyUMAq7YFt4uj4vbbcJ8TK7e40Wu4c687sZAtW7ju5vuvVZoDpTpNS876WjvDPbgbyZi5W8ARA3O5RTwrr51la8Hat9u/mHrDx+YjG8NNLHPNsOkTtg6ZU8kaqZOMsgJ7zatZ+7pr3Vu4Tg9LwmEV05ahpJvAiYwTzVqb675GHvPPjfEL1VpeG7FwFIu7q2k7xoGby7kO/8O6K7u7zQ05a8u4FnPHWtJ7tTpNS7AoxgvMtMmbzH8pq8ypH8vMtMmTyseow7Ef6gPL89kTwetcQ6Lnc8PH2Oo7sjwaU6/eEqPaaanTuXVM+8pAvzu6MUrTz1WSA7h5yePOxuXTx8YSS8Z8DKOpej+byCQZM7b804PCtsaLm+aYO8pcaPvELi3LqFOWY8sMtQu/h95Ttlv727lqyzvH3dTbz23oM8KhN3PMTnRryHF7u7O4ZEPClrW7t40ew8f0AGvSIZCjyR+cM7l1TPPGG0abxmyQS8HAPivLw8BD3/Zxu8WlhRPCv6hTuC32e8fSz4PFn/3zyT15i8baA5PDMqrDs5Chu89yR0PMvqbbz31cm7hzpzvCzFWbzrS6U7Cuh4u73kHzyEFi68okCfu1em7jtbAO08XAq0PMfyGrzwIU06FKfJuyC2UbyUBJi8kfnDvGpp8zkVsRA9yUsMvWG06TxDGBY8iCECvWG0aTz8iLk6U6RUvEY82zyY/Gq8ivacPF68ljyIRLo8Lnc8PJyMorw8ZBm8K2zoO3jR7DxH5PY7hTnmvFyPFzu4XaK8tiZcuspC0jyNRtQ8fGGkPKrIKbz9tbg7fSz4O8sgJ70WhZ48rXHSO69PJztM8Fc8cPq3vCVpwTzveTG3IV5tOWho5rxap/u51IYGPB2r/Tsnufg79SPnvHfapjvOROw7JEYJPIiT5Dxajoo7uoqhvO2klrulZOQ8GKljvGDplTxM8Fe8IV5tOsQTuTwU3QK7vLcgvMlLDDwuxua7LKKhOsPwAD1TBgC9zet6uAz8hrzFbKq8iexVPFGjx7xZNRm8E7CDu1Sum7v6Wzq9RRkjvBtbRrz3JHS7gW2FO7cwI7qsyba689MvvDkKm7vou+273q1yPM//iLvyej683Yq6uw79kzu4XaK6Tyeeu3TZGTpHlUy8TKGtPMA0VzzWUVo7ElcSOpu4FDy62cu7XbJPupgypDw9h1E8+AsDPJNJ+7oLo5W8RZS/PHcpUTsgZyc8Au4LvFPz/jstz6C89LGEvBqHODzTWYc7VExwPPVZoDyZVVy8ctgMPMp4C7ydCMw8/Ii5O1D7q7thFpW8oROgu0SK+Ds/ObS7aMqRuy6jLjssJ4W8NrAcPaoX1LtkF6K87G5dvJ6wZzsuxua6LR5Lux0NqTvG6FO681iTPFSum7wv/B88MiBlvAqZzrxq9xC96PEmOleDtjonufi6RRkjvG6qgLxaCSe8nBEGupFIbrzhuEY8DimGuoOaBDw41OG7Fy06OmUO6LwlacG7LfuSvCkcsbvANFc7rcD8u/2SgDwtbfU7/onGPI7u77v1I+e8CJhBO+q8+js00sc7Y76wO/8xYrxg6ZU8XLuJvBSnSbzteCQ7neWTPFuxQrweBG+7si6JPOlKmDzKQlI7P+qJumtQgjsahzi8Hav9PJr997xeWms8pxZHvNQBozt/jzC7nbmhvH26lbrhuMY7+TgCOoghArxCkzK8iU4BPO4gwLt7K2s8KHSVvMKXjzxmnZI8eduzvE5JSbxkZky8heq7PCbCsjs7Nxo8KnUivBiGq7yUU8K8KsTMu6XGD73ZDQQ86xXsvFlhizxzz9I66GxDvAXDJruNRlQ7cx79O3lgFzzExI67kSW2O7Ym3LwilKY84QfxOyBnp7zlumC8zENfu2S19rvqbVA5BD7DOyZziLzpFN+7SfgEPTiFt7x73MC84JWOueCuf7zHQcW7UIAPPME+njxfs9y8VQcNuyrEzLuNRtS8IV5tPDTSRzyhsXS81AEjvNF7sjwyghC72avYuyYRXbwC7gu9g5oEO+xu3boO/ZM8HQ2pO29+jrz8OY+8FFgfPKi+4jrzWBO8u+OSPEzwVzy/PRE647lTPLSqMjzu/Qe7eDOYOzcJjrxI7r08JbjrPECSJTzdirq7S/mROjN5VjxLJYQ6EqY8uveGn7w41OE7zZzQu+0Weby6KPY8pcYPvEdGIjxTVao8C0FqvGNvhjsS9eY72gRKvJZdibwCaag8eSreOjvVbrzG6FM8w45VPFGjxzzQRfm7/+I3PP1//7snufg8UErWOzsLKLwCjOA6xjf+O+MIfrsud7w7ju7vPBGDhLwAi9O7Yw3bPA/0WTz1I+c8Lnc8PB1cU7ymmp07wJYCvDIgZTxtoLm8V1fEPJtW6bvfBmQ8EU1LvDGugrtaWFG8rSIoPLB8pru5gFq8PTgnO8eQbzxAvpe5rhnuPM5EbLvTWYe8hOB0uleDtrz6fvI87m9qu4FthTxTBoA8TKEtvP+TjbyRSG68aPYDvEKTMrwWqNa7dAUMPR2rfTptzCs8eIJCO+AQqzzVWhS82atYPCrEzDtTVaq7wpePPG8cYzzRLIi8w91/vKtwxbtgW3g84F/VOqJskbnS1KM8GWSAvIfryLe1L5a7JWnBPC7G5rtzz1I7U1UqOnQoxDseBO87BeZePMpC0jvFj2I6l4oIPFoJp7zrS6W8clOpvF0B+rpKoKC7YOmVOo7ub7rHHo27DHcjPJtW6brnSYs7Sj51vNX46LyAFBS8dAUMO8QTuTzeXsi8ddDfO8rHtTy7Mr27ATNvPHkq3joKzwe9wY3Iulb+Urx4gkK85hNSOzcsRrxXpu67aZ6fO2tQgjx2Bhk9eNFsvD04pzvAapA7avcQPC0eSztEwDE8zvVBOjGugjuovmK8yyCnvH4Thzy1A6Q884SFPHd4+zvUn/c81FDNu87SibsImMG8NjUAO47ubzy82lg82127u0K/pDsh7Ao8avcQPFb+UjqCkL27mwc/PKRtnrsJdhY868bBvJOrpryI9Y+70NOWPDOvjzuExwM8BGq1OyYRXTvbDhE9gBSUPAHkxDzBjUi8xR0AvPXUvLwYCw+6jp/FOlWlYbybBz+7fDUyvNhSZzz0e8u5ECqTPKQL8zrZiCA5jcs3POqjiTtudMc7Dpvou3xhJLxRo8c8lwWlOfrgHbqaX6O7j/g2vGC9ozvAapC8ljEXPFKtDry0qrK7t7WGvCyiITz/Z5u8edszvINukry2Jlw8yyAnPFDPuTsaOA47qk0Nu5fZMryDbhI8AeTEvLnihTzRABa8geghPCC20bpwJiq8swIXPEdGojtmGC87FgqCPK4ZbjymH4E8dHfuusLmubxXNIw8jZV+PJ5hPbusGGE8nmG9urUDpDqIIYI8SyUEvMLmuTySod+72OAEPIztYroMSzG80XuyvG3MK7z4fWW8S/mRO3Crjbwsxdk7xJicPF0B+ru4J+k8wDTXO86mlzwuxua7dFQ2vJsHvzuz1qS7a5+sPEzw1ztNdTu7TM2fvNpT9Ln1WaA86RTfO08nnru/PZE7neUTvMHDAbwS9Wa8Zp0SvEbtsDpQrAE95Zeou1sA7TvYA727TPBXOrYm3DsbqvA7WljRvGhFrrv6L0g82AO9vIA3zLwA2v07+YcsvFbbmjtWTf07gkGTuRGcdbyCkL08HeE2uyAF/DuvIzU889MvvJSi7DmBvC88YUKHOz/qCb3zhIU8dgYZvKGYgzt4B6Y7c6waO5heFrtR2YC8cic3PDwu4DolaUG8F7IdvF0B+jvlHIy7JbjrOxUAO7xyduE85mJ8PMsgJz3luuC7iZ0rPMM/K7yPR+E7BZe0O/46nDyDmoS65brgvKcWR7wSpjy8Z3Ggu4wjnDzw/pQ7U/P+u4xPjrwx/ay7gpA9PMp4CzybuJQ8/TDVPBw5G7xMzZ+8mVXcO4lOgTrNnFC8J7l4OnrSebw+L+06si6JtiW4azwPVoW8aPaDPEXjabpuqgA8amlzPHIntzxxf5s6aXKtO8C5uroHdYm85GHvvHqDTzqV2KU8V6ZuO1Hy8TtMzR88r0+nPBipYztZYQs9cic3O+Jg4rwP9Fm6CfEyvEZyFLzLICc8gt/nvHTZmTwUWJ+8YFv4uwfEMzsBM288YFt4vMrzpzsdXNO8KhN3uxaoVrxNdbu8uTGwPMbo07x0KMS7ElcSOyx2LzzLTBk8NiL/O1tiGD1fkCQ8YL0jvLJ9szymvdU6G6pwvKtwxbvpSpi8NXpjvGvCZLx4gkI860slvONqKToTsAM8FU9lPGvCZDyhE6A8XQF6uwpKpLqWMZe7lAQYPMqRfDvPTrO8DUJ3PDvV7jtIPWi8M/65uvgLAzwqdSI8obH0vPLJ6LmkvMg76PEmvF9ksjtVVjc87NAIPP1mDryYMiS8w47VvCK33rolacG8D6WvPA/RITtmnRI9sn2zu6hMgDs4sSk8AZUaO4oij7tRo0c81J93u4nsVbs41OE7VP3Fu5vkhju1fkA8DaQivN9oj7y7gWc7FlksvMSYHDt5Kt66u14vOqpNjbs6LdO79AAvvDAf2DueYT27fhMHPPAhTT0mwrK8o8WCPBo4Dr331cm77tEVO9azBbybBz+8Au4LvbvjkjyUf7S7mDKkvJCg0jsHP9A8jcu3PBnfnDzK86e89HvLO49H4bsYqeM8+YcsPcz0tLpLSLy7JEYJvGGRsbz0sQQ8KsRMPM71wTzNnFC8GIYrO36F6bvoHRm8U1UqvESKeLywGns8LR5LvGEWlTxy2Iy8XuiIPI2V/rtEivi8W7FCvMeQ7zyn84676xVsvElHL7y21zE8IpQmvVMGgLzaBEq7rnuZPLV+QLwLo5W811uhu05/grszeVa8veQfvELiXDxQz7m8fSx4vGz4nbvzIlo7Au6Lu35iMT38DR08FlmsPFUHjbYdq328omwRPHJTqTq5gFo8XQF6vJHWi7tU2o27AIvTu9WpPjtG7bC8E/8tvH+7IjygawQ9FN2CO4nsVTwzedY60Z7qu3q5CD28iy48ldilvHiCwrwEPsM7JsIyvMbFG7x4gsI8swIXOwLui7rhuMY8VP1FO8OO1TtrwuQ78P4UuZwRBjy1zeq8M9sBvHTZmTzRe7I8PGQZvXAmqjsKmU48eDMYPLB8JjwlaUG7hW+fPKYfAT16g085ADwpPFgImrtkZky8wJaCvErvSjw0g528NiL/uzqytrs4hbe7jiSpuRfeDzyP+DY82Yiguheynbs6sjY8hzrzPJkGsroF5l68NyxGPKRtnrxoaOa7fIRcPOJgYrvLm8O7mbeHuWtzOrva4ZE60SyIPD+I3rzsH7O8p2Xxun+7orx85gc9/NfjvGVEIbucYDA8+dZWvGr3ELyZBrK73pQBvKXGDzxvUhw87tEVPXuNljxEO068/Ig5vAqZTrtCRAg8k/rQO8OOVbvqvPo7eWAXvL3kH7xTBoA8a8JkPJ0IzDy/22U8hpJXu2NvhjzlHIw7cR3wO0KTMrysyba6ILZRO4oijzxpnp+7Yw1bPDotU7ySfqe8Zp2SvK4ZbjwAPKm8UPurutoESjy3tYY8z51dPMDlrDyesOe6o5kQPGhoZjve46s86h4mvJejeTtmyQS8V4O2OnJ2YbxdAfq8ldilvJdUTztdsk+6ivacvJ5hPbx60vk8/ZIAPDdYuLr11Ly8GjgOOq9y37ooSKO8x203u/eGH735OAK8E7CDvH26lbx8hFy6oZgDPHcp0TlXNIw7Xlrru6Nj1zvK8yc8QTrBO9gvrzrKQtI88NKiPJheljxpci08mwc/PLKppTuC3+c8nBEGPLComDxe6Ii72OAEvcubQzxR8vG7M9sBu+Nqqbw2In8847lTvI+pjLwsdq88/TBVPIOahLwd4bY8B3WJOeq8ersaOI48eSpevOO5Uzwh7Io8nK/auyPBpTy2Jty7obH0PNoEyjutcdK8M3lWO7UDpLwx/Sw8bsPxO1b+0jvNTSa7Ef4gvF5aa7wgtlG7l6N5vGpp8zx/3lo8DfNMvDqytjwWhR68l1RPu1jcpzsjX/q8AeREvBFNyzs71W688yJavLMlz7uyqaU84MEAO6wYYTvtpBY8B/AlPPZ8WLwUWB+8mq7NO5Dv/DyR1os7xBM5vKQLczrZXK67Uvy4O4wjHL0tz6C8pAtzPE5/Ajz2fNi7TFKDvHjR7LsPpa+7K2zovNmIoDsYhis8mF4WvOIRuLzmYny8tKqyug+lLzyKRcc699VJuwro+DwcZY28k9eYPF2yTzzORGw8erkIvJdUT7zft7k8eDMYPA5MvrtIPei7ddDfu/nW1rrelIE855i1OziFtzuT+tA7SJ8TPFT9xbw4hTc7McdzOqtwxbxktfY7yse1u0dGIrrKkXw6pWTku+Rh77sAi9M8omwRu/ckdLwCjGA8raeLuwI9NrusGGE8vTNKPNpTdDwC7ou8MXjJvF68ljxBOkE8wz8rO1Pz/js1K7m81FBNO4lOgbuBbYU8cnZhO6hMgLy2iIc76xVsvPbegzonak68e9zAuUg9aLyf5iC7q79vPDMqLLpAvhc8zvXBOwtBajzZDQQ7GQJVPFJLYzz+icY88NIiuyx2LzzobMO7+n7yPFpY0TtOzqw8p8ecO7za2DuXo3m8CJjBuxWxkDvRT8C76GxDOioTdzwqoRS8aSODPELiXDreD548RZS/OYhEOrkCjOC8hpLXt6PFgjplvz28rspDO5JStTtlv728Hw42PCZHFruUU0K8/A2du7UvFj3u0ZU8fucUvFyPF7yxc+w7bPidPMGNyLr64J28gW0FPUuX5jw+4EI868ZBPOqjiTwdXNO51/l1u7oo9rwOKYY76m1QPH26FTzB3HK7yelgPAXDJjwjX/o7jUZUvCfvsbv812O81J93vOG4xjtHlcy4aPYDvPpbOrxfs1y6vmmDvPPTrzy4iZQ75mL8OiXupLwU9nO8Z8DKOtRQzbyFm5G7pcYPPG0bVrynx5w7mzMxu+4gQLktHku7rhnuOUWUvzwEjW08WljROubEp7zTqDE84hG4O56wZznZDYQ8hxe7PPfVyTwiGQq8AZWaO1RM8DxE7CO7Onz9vLgn6TvYA707VQcNO36F6bgBlZq6x5DvPNGeajwMSzE8HrXEu8eQbzwN88y6mv13O7vjEj37tCu9c8/Su1eDNrzMeZi7O9XuO3Ed8Dtpcq08pRW6u3YGmbyFbx+7aPYDOZSi7LutwPy7hzrzOuq8+jtfs1w8ZGbMutuJrbwYWrm5y5tDvATvGDwR0i47Ch4yul43szwyVp68GIarPNUuIrv51lY7KhP3OngzmLzNTaY7IrfevMkfmrvyer68K84TPBkCVbumvVU8ypH8uizFWbxpI4M7KEgjOpGqmbs/ObS6IxDQO4fryDzBjci6zHkYvDF4ST2Rqpm7zPQ0uzqyNju/jLu7FoUevVoJJz2nZfG7Au6Luzcsxrw4hTc82gRKPPIrlLyR1ou7u16vuzxkGbxwdVQ8GbMqvJgyJDx0KMS8V6buOv/iN7s4hbc7KqGUOzd7cDp4Bya8h+vIPNF7Mjtcu4m7IrdePLhdojwnoIe7A8IZvGnB17kBELe7NSs5vAc/UDyXigg8B3WJu5yv2rt5YJe8vRASOLIuCTxE7CM8rMk2PHD6Nztrczq8JsIyOhtbRrzvKoc826zluw4pBj3+2HC8rhluPGBbeLz9koC7/A2dPHxhpDqn8468u+MSPIhEujxI7r07+Yesuy53vDsygpC7/NdjPN7jqzzCl4+8jp9Fu/J6vjvzhIW8zHkYPJYxFzwVT+W7AZWavN8G5Lusyba5HZKMOjixKTySUrU750kLPNEAljx73MC8JbjrO9f59bniYGK8kO98OzvVbjxudMe44F/Vu8Pd/zpNJpG8FbEQvIqU8bqZt4c6PuDCO6dlcTy1zeo6Pb0Kug1C9zuHnB48EZz1O3YGGTxVVjc7vz0RvEK/pLoWCgI8FgqCvNGe6rwYNwG9LqMuuwrPhzsOTL67YbTpu+Jg4jp9LHg8AzR8vGj2g7wGHJi8zPQ0PBOwA7mNfI26hpLXOuryM7x02Rk8wY3IvMTnRrxU/cU6ghWhuyZziLu745K7nBGGPF5aazxG7bA8hOD0PJJ+p7rHbbe88iuUu+QSxTuAN8w7vz2RvPNYE7uqyKk8sn2zO250Rzxap3u8XrwWvC6jLjxzrJo8cCaqOxAqkztg6RU7QOHPOjEpnzzmE1I8qsgpPPovyLxlDui7LHavPFrdtLs201Q8/A0dO60iqLsv/J+8k9eYPDawHLykC3M8tlyVPBT287wOTD48jCMcO3fapjvCNWS7GbOqvMCWgrxi6iI8XugIPaZuK7yC3+c7R5XMO9jgBLyesGe7rHoMu5/mIDwqE3e8T6I6vDp8/TwyghC8AmkoPFHZgLtOfwK8D/RZvCZziLzpFN87oROgPFTaDbnQRXm8dHfuu6GxdLyaXyO7yyCnu/0wVTx60vk71VqUO0fLBbwOeDC8wpcPPVsA7TtVpWG8QeuWu1gIGrxqGsk7VaVhO1dXRLxeC8E8ZmfZOxtbxryXBaW8XAo0Ow54sLu82ti6Wt00vEUZo7pfs9y4f0CGOyPBJTvsbt08rnuZvO0WeTt3eHu8iU4BvJH5QzxbAG27d3h7POoepjzxV4Y7AzT8uxP/LTyeYb08bVGPvLmAWjpGPNs8CfEyu3/eWrz1I2e75O8MPLN0eTyUouw8YbTpOxHSrrxWr6g8Tn8COu5MsjxbscK647lTvLSqMjzhPaq6JkcWPLKpJTteNzO8ONThO67KQzxe6Ag8xY/iPMTnxrspHDG7amnzO7Kppbpap/s8IGenPCts6LsyVh651FBNul9ksjvqowk8Zp2SuwhJFzxVgik87B+zvEmWWbxO+p67tlyVPNy2rDwz2wG9FN0CvWskELzLm0M8racLPZkGMjsmR5a70QCWuXgzmDx0d247si4JvJm3BzxP8WS8JhHdO3XQXzusGOG7JkeWPAoesryLyio7YRaVvElzobyP+LY8NXpjPBeyHTt73EC8omyRvBlRfzzExI68dgaZu3srazuHOnO8PYfRuldXxDtjvjC8sQEKvAtB6jyQ73w7RIr4O/FXhrqtp4u7UkvjvNFPwDxrczo7V1dEu26qgDtW/tK8xJgcvF5aa7zExA67b824vHdfCrz2fFg7XI+XvBwD4ryXVE88",
        "thumbnail": "https://upload.wikimedia.org/wikipedia/en/d/d2/Back_to_the_Future.jpg",
        "thumbnailWidth": 0,
        "thumbnailHeight": 0
      },
      "second": 0.463297247887
    }
  ]
}
```

### HTTP File

Requests can easily be made with the request.http file included in the project.
This file contains all the endpoints and their respective request methods.
You can use it with any HTTP client that supports .http files, such as Intellij, Postman or Insomnia.


## Implementation Details

Vector similarity search is implemented using Redis OM Spring, which provides a simple and efficient way to interact with Redis. The following sections detail the implementation of the movie recommendation system.

### Project Setup

1. **Add Redis OM Spring Dependencies**
   Add the following dependencies to your `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.redis.om.spring</groupId>
       <artifactId>redis-om-spring</artifactId>
       <version>0.9.10</version>
   </dependency>
   ```

### Entity Definition

Create a Movie entity with Redis OM Spring annotations:

```java
@RedisHash // Defines a hash within Redis
public class Movie {

   @Id // ID is created automatically by Redis OM Spring as ULID
   private String title;

   @Indexed(sortable = true) // Creates index for field
   private int year;

   @Indexed
   private List<String> cast;

   @Indexed
   private List<String> genres;

   private String href;

   // Creates vector for the synopis of the movie
   @Vectorize(
           destination = "embeddedExtract", // the field to store the vector
           embeddingType = EmbeddingType.SENTENCE, // type of embedding (sentence, face, image, or word)
           provider = EmbeddingProvider.OPENAI, // embedding provider (If empty, uses Hugging Face Transformers)
           openAiEmbeddingModel = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_LARGE // OpenAI embedding model
   )
   private String extract;

   // Creates index for the vector
   @Indexed(
           schemaFieldType = SchemaFieldType.VECTOR, 
           algorithm = VectorField.VectorAlgorithm.FLAT, // search algorithm (either FLAT or HNSW)
           type = VectorType.FLOAT32,
           dimension = 3072, // dimension of the vector (must match the embedding model)
           distanceMetric = DistanceMetric.COSINE, // distance metric (either COSINE or EUCLIDEAN)
           initialCapacity = 10
   )
   private byte[] embeddedExtract;

   private String thumbnail;
   private int thumbnailWidth;
   private int thumbnailHeight;
    
    // Getters and setters
}
```

### Repository Setup

Create a repository interface extending `RedisEnhancedRepository`:

```java
@Repository
public interface MovieRepository extends RedisEnhancedRepository<Movie, String> {}
```

### Service Implementation

Implement the search functionality in a service class:

```java
@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
   
    // Entity Stream is a Redis OM Spring abstraction that allows you to create a stream of entities
    
    private final EntityStream entityStream;
    private final Embedder embedder;

    public SearchService(EntityStream entityStream, Embedder embedder) {
        this.entityStream = entityStream;
        this.embedder = embedder;
    }

    public List<Pair<Movie, Double>> search(
            String query,
            Integer yearMin,
            Integer yearMax,
            List<String> cast,
            List<String> genres,
            Integer numberOfNearestNeighbors) {
        logger.info("Received text: {}", query);
        logger.info("Received yearMin: {} yearMax: {}", yearMin, yearMax);
        logger.info("Received cast: {}", cast);
        logger.info("Received genres: {}", genres);

        if (numberOfNearestNeighbors == null) numberOfNearestNeighbors = 3;
        if (yearMin == null) yearMin = 1900;
        if (yearMax == null) yearMax = 2100;

        // Convert the query into a vector using the embedder bean provided by Redis OM Spring
        byte[] embeddedQuery = embedder.getTextEmbeddingsAsBytes(List.of(query), Movie$.EXTRACT).getFirst();

        SearchStream<Movie> stream = entityStream.of(Movie.class);
        return stream
                .filter(Movie$.EMBEDDED_EXTRACT.knn(numberOfNearestNeighbors, embeddedQuery)) // Filter by nearest neighbors of the embedded query
                .filter(Movie$.YEAR.between(yearMin, yearMax)) // Hybrid search
                .filter(Movie$.CAST.eq(cast))
                .filter(Movie$.GENRES.eq(genres))
                .sorted(Movie$._EMBEDDED_EXTRACT_SCORE)
                .map(Fields.of(Movie$._THIS, Movie$._EMBEDDED_EXTRACT_SCORE))
                .collect(Collectors.toList());
    }
}
```

### Key Features Explained

1. **Vector Embeddings**
   - Converts text into numerical vectors
   - Captures semantic meaning
   - Enables similarity comparisons

2. **Vector Indexing**
   - Efficient storage of high-dimensional vectors
   - Fast similarity search
   - Support for various distance metrics (cosine, euclidean)

3. **Semantic Search**
   - Understands context and meaning
   - Handles synonyms and related concepts
   - Provides relevant results beyond exact matches

### Additional Resources

- [Redis OM Spring Documentation](https://github.com/redis/redis-om-spring)
- [Redis Search Documentation](https://redis.io/docs/latest/develop/interact/search-and-query/)